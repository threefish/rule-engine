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

package cn.xjbpm.rule.engine.definition.model.nodes;

import cn.xjbpm.rule.engine.definition.model.enums.NodeType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 邮件节点
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EmailNode extends Node {

    private String credentialId;
    /**
     * 收件人（多个用逗号分隔）
     */
    private String to;
    /**
     * 抄送（多个用逗号分隔）
     */
    private String cc;
    /**
     * 密送（多个用逗号分隔）
     */
    private String bcc;
    /**
     * 邮件主题
     */
    private String subject;
    /**
     * 内容类型
     */
    private ContentType contentType = ContentType.TEXT;
    /**
     * 邮件内容
     */
    private String content;
    /**
     * 附件路径（多个用逗号分隔）
     */
    private String attachments;
    /**
     * 是否使用SSL
     */
    private boolean useSSL = true;
    /**
     * 回复地址
     */
    private String replyTo;

    @Override
    public NodeType getType() {
        return NodeType.EmailNode;
    }

    public enum ContentType {
        /**
         * 纯文本
         */
        TEXT,
        /**
         * HTML
         */
        HTML
    }
}
