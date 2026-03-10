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
import cn.xjbpm.rule.engine.definition.model.nodes.EmailNode;
import org.springframework.util.Assert;

/**
 * @author 黄川 huchuc@vip.qq.com
 */
public class EmailNodeValidator extends BaseNodeValidator<EmailNode> {

    @Override
    public void check(EmailNode node) {
        super.check(node);
        Assert.isTrue(StringUtils.isNotBlank(node.getCredentialId()), "SMTP凭据不能为空");
        Assert.isTrue(StringUtils.isNotBlank(node.getTo()), "收件人不能为空");
        Assert.isTrue(StringUtils.isNotBlank(node.getSubject()), "邮件主题不能为空");
        Assert.isTrue(StringUtils.isNotBlank(node.getContent()), "邮件内容不能为空");
    }
}
