/**
 * Copyright 2025 threefish.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.xjbpm.rule.common.utils;

import cn.xjbpm.rule.engine.runtime.model.credentials.EmailCredential;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 邮件工具类
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Slf4j
public class EmailUtils {

    /**
     * 发送邮件
     *
     * @param credential  邮箱凭据
     * @param to          收件人（多个用逗号分隔）
     * @param cc          抄送（多个用逗号分隔，可为空）
     * @param bcc         密送（多个用逗号分隔，可为空）
     * @param subject     邮件主题
     * @param content     邮件内容
     * @param contentType 内容类型
     * @param attachments 附件路径（多个用逗号分隔，可为空）
     * @param useSSL      是否使用SSL
     * @param replyTo     回复地址（可为空）
     */
    public static String send(EmailCredential credential, String to, String cc, String bcc,
                              String subject, String content, String contentType,
                              String attachments, boolean useSSL, String replyTo,
                              String baseAttachmentPath) throws Exception {
        Properties props = buildProperties(credential, useSSL);


        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(credential.getUsername(), credential.getPassword());
            }
        });

        MimeMessage message = new MimeMessage(session);

        setFrom(message, credential);
        setRecipients(message, to, cc, bcc);
        setReplyTo(message, replyTo, credential);
        message.setSubject(subject, "UTF-8");

        if (StringUtils.isNotBlank(attachments)) {
            setMultipartContent(message, content, contentType, attachments, baseAttachmentPath);
        } else {
            setSimpleContent(message, content, contentType);
        }

        Transport.send(message);

        String messageInfo = String.format("邮件发送成功: 收件人=%s, 主题=%s", to, subject);
        log.info(messageInfo);
        return messageInfo;
    }

    /**
     * 构建邮件属性
     */
    private static Properties buildProperties(EmailCredential credential, boolean useSSL) {
        Properties props = new Properties();
        props.put("mail.smtp.host", credential.getHost());
        props.put("mail.smtp.port", credential.getPort());
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.connectiontimeout", "30000");
        props.put("mail.smtp.timeout", "60000");

        if (useSSL) {
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        } else {
            props.put("mail.smtp.starttls.enable", "true");
        }

        return props;
    }

    /**
     * 设置发件人
     */
    private static void setFrom(MimeMessage message, EmailCredential credential) throws Exception {
        if (StringUtils.isNotBlank(credential.getFromName())) {
            message.setFrom(new InternetAddress(credential.getFrom(), credential.getFromName(), "UTF-8"));
        } else {
            message.setFrom(new InternetAddress(credential.getFrom()));
        }
    }

    /**
     * 设置收件人、抄送、密送
     */
    private static void setRecipients(MimeMessage message, String to, String cc, String bcc) throws Exception {
        message.setRecipients(Message.RecipientType.TO, parseAddresses(to));

        if (StringUtils.isNotBlank(cc)) {
            message.setRecipients(Message.RecipientType.CC, parseAddresses(cc));
        }

        if (StringUtils.isNotBlank(bcc)) {
            message.setRecipients(Message.RecipientType.BCC, parseAddresses(bcc));
        }
    }

    /**
     * 设置回复地址
     */
    private static void setReplyTo(MimeMessage message, String replyTo, EmailCredential credential) throws Exception {
        if (StringUtils.isNotBlank(replyTo)) {
            message.setReplyTo(parseAddresses(replyTo));
        }
    }

    /**
     * 设置简单内容（无附件）
     */
    private static void setSimpleContent(MimeMessage message, String content, String contentType) throws Exception {
        if ("HTML".equalsIgnoreCase(contentType)) {
            message.setContent(content, "text/html; charset=UTF-8");
        } else {
            message.setText(content, "UTF-8");
        }
    }

    /**
     * 设置复合内容（有附件）
     */
    private static void setMultipartContent(MimeMessage message, String content, String contentType, String attachments, String baseAttachmentPath) throws Exception {
        Multipart multipart = new MimeMultipart();

        MimeBodyPart textPart = new MimeBodyPart();
        if ("HTML".equalsIgnoreCase(contentType)) {
            textPart.setContent(content, "text/html; charset=UTF-8");
        } else {
            textPart.setText(content, "UTF-8");
        }
        multipart.addBodyPart(textPart);

        String[] attachmentPaths = attachments.split(",");
        for (String path : attachmentPaths) {
            String trimmedPath = path.trim();
            if (StringUtils.isNotBlank(trimmedPath)) {
                File file = Paths.get(baseAttachmentPath, trimmedPath).toFile();
                if (file.exists()) {
                    MimeBodyPart attachmentPart = new MimeBodyPart();
                    attachmentPart.attachFile(file);
                    multipart.addBodyPart(attachmentPart);
                    log.debug("添加附件: {}", trimmedPath);
                } else {
                    log.warn("附件文件不存在: {}", trimmedPath);
                }
            }
        }

        message.setContent(multipart);
    }

    /**
     * 解析邮件地址
     */
    private static Address[] parseAddresses(String addresses) throws Exception {
        if (StringUtils.isBlank(addresses)) {
            return new Address[0];
        }

        String[] addressArray = addresses.split(",");
        List<Address> result = new ArrayList<>();
        for (String addr : addressArray) {
            String trimmed = addr.trim();
            if (StringUtils.isNotBlank(trimmed)) {
                result.add(new InternetAddress(trimmed));
            }
        }
        return result.toArray(new Address[0]);
    }
}
