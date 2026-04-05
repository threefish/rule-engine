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

package cn.xjbpm.rule.engine.definition.validator;

import cn.xjbpm.rule.common.utils.StringUtils;
import cn.xjbpm.rule.engine.definition.model.nodes.FeishuNode;
import org.springframework.util.Assert;

/**
 * 飞书消息节点验证器
 *
 * @author 黄川 huchuc@vip.qq.com
 */
public class FeishuNodeValidator extends BaseNodeValidator<FeishuNode> {

    @Override
    public void check(FeishuNode node) {
        super.check(node);
        Assert.isTrue(StringUtils.isNotBlank(node.getCredentialId()), "飞书应用凭据不能为空");
        Assert.isTrue(StringUtils.isNotBlank(node.getMessageContent()), "消息内容不能为空");

        if (node.getOperationType() == FeishuNode.OperationType.REPLY) {
            Assert.isTrue(StringUtils.isNotBlank(node.getMessageId()), "待回复的消息ID不能为空");
        } else {
            Assert.isTrue(StringUtils.isNotBlank(node.getReceiverId()), "消息接收者不能为空");
        }
    }
}
