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

package cn.xjbpm.rule.engine.runtime.behavior;

import cn.xjbpm.rule.common.constant.RuleFlowConstant;
import cn.xjbpm.rule.common.utils.EmailUtils;
import cn.xjbpm.rule.engine.aviator.AviatorContext;
import cn.xjbpm.rule.engine.aviator.AviatorExecutor;
import cn.xjbpm.rule.engine.definition.model.nodes.EmailNode;
import cn.xjbpm.rule.engine.runtime.model.FlowContext;
import cn.xjbpm.rule.engine.runtime.model.credentials.EmailCredential;
import cn.xjbpm.rule.properties.RuleProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * 邮件节点行为处理器
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Slf4j
@SuppressWarnings("all")
public class EmailNodeBehavior implements NodeBehavior {

    private final EmailNode node;

    public EmailNodeBehavior(EmailNode node) {
        this.node = node;
    }

    @Override
    public void execution(FlowContext context) throws Exception {
        if (RuleFlowConstant.DEMO_MODE) {
            handleDemoMode(context);
            return;
        }
        RuleProperties ruleProperties = context.getBeanContextManager().getRuleProperties();
        String baseAttachmentPath = ruleProperties.getAttachmentPath();

        EmailCredential emailCredential = context.getBeanContextManager().getCredentialsManager().getEmailCredential(node.getCredentialId());

        log.info("执行邮件节点: {}", node.getName());

        EmailExecutionContext execContext = resolveExpressions(context, baseAttachmentPath);

        String result = sendEmail(emailCredential, execContext);

        Map<String, Object> resultMap = buildResultMap(result, execContext);
        context.put(node.getId(), resultMap);

        if (context.isDebugModel()) {
            context.addTraceLog(node.getId(), "Email result: {}", new Object[]{result});
        }
    }

    /**
     * 一次性解析所有表达式，避免重复执行
     */
    private EmailExecutionContext resolveExpressions(FlowContext context, String baseAttachmentPath) {
        Map<String, Object> variable = context.getVariable();
        return EmailExecutionContext.builder()
                .to(evaluateString(node.getTo(), variable))
                .cc(evaluateString(node.getCc(), variable))
                .bcc(evaluateString(node.getBcc(), variable))
                .subject(evaluateString(node.getSubject(), variable))
                .content(evaluateString(node.getContent(), variable))
                .attachments(evaluateString(node.getAttachments(), variable))
                .replyTo(evaluateString(node.getReplyTo(), variable))
                .baseAttachmentPath(baseAttachmentPath)
                .build();
    }

    private String evaluateString(String expression, Map<String, Object> variable) {
        if (expression == null) {
            return null;
        }
        return AviatorExecutor.evaluateString(AviatorContext.create(expression, variable));
    }

    /**
     * 处理演示模式
     */
    private void handleDemoMode(FlowContext context) {
        context.addTraceLog(node.getId(), "演示模式不允许执行,已跳过");
        Map<String, Object> data = new HashMap<>();
        data.put("data", "演示模式不允许执行");
        data.put("errorCode", 0);
        context.put(node.getId(), data);
    }

    /**
     * 发送邮件
     */
    private String sendEmail(EmailCredential emailCredential, EmailExecutionContext execContext) throws Exception {
        Assert.hasText(execContext.to, "收件人不能为空");
        Assert.hasText(execContext.subject, "邮件主题不能为空");
        Assert.hasText(execContext.content, "邮件内容不能为空");

        return EmailUtils.send(
                emailCredential,
                execContext.to,
                execContext.cc,
                execContext.bcc,
                execContext.subject,
                execContext.content,
                node.getContentType().name(),
                execContext.attachments,
                node.isUseSSL(),
                execContext.replyTo,
                execContext.baseAttachmentPath
        );
    }

    /**
     * 构建结果映射
     */
    private Map<String, Object> buildResultMap(String result, EmailExecutionContext execContext) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put(RESULT, result);
        resultMap.put("to", execContext.to);
        resultMap.put("subject", execContext.subject);
        resultMap.put("contentType", node.getContentType().name());
        return resultMap;
    }

    /**
     * 邮件执行上下文，缓存已解析的表达式结果
     */
    @lombok.Builder
    @lombok.Data
    private static class EmailExecutionContext {
        private String to;
        private String cc;
        private String bcc;
        private String subject;
        private String content;
        private String attachments;
        private String replyTo;
        private String baseAttachmentPath;
    }
}
