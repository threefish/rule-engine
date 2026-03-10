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

import cn.hutool.core.collection.CollUtil;
import cn.xjbpm.rule.common.utils.StringUtils;
import cn.xjbpm.rule.engine.definition.model.nodes.DBNode;
import org.springframework.util.Assert;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/30
 */
public class DBNodeValidator extends BaseNodeValidator<DBNode> {

    @Override
    public void check(DBNode node) {
        super.check(node);
        Assert.isTrue(StringUtils.isNotBlank(node.getCredentialId()), "链接凭据不能为空");
        Assert.isTrue(StringUtils.isNotBlank(node.getSql()), "SQL语句不能为空");
        Assert.notNull(node.getExecuteType(), "执行类型不能为空");
        if (CollUtil.isNotEmpty(node.getParams())) {
            for (DBNode.Param param : node.getParams()) {
                Assert.notNull(param, "参数字段和参数表达式不能为空");
                Assert.isTrue(StringUtils.isNotBlank(param.getField()), "参数字段不能为空");
                Assert.isTrue(StringUtils.isNotBlank(param.getExpression()), "参数表达式不能为空");
            }
        }
        if (node.getExecuteType() == DBNode.ExecuteType.SELECT_LIST) {
            Assert.isTrue(node.getMaxResultCount() > 0, "最大结果响应数量必须大于0");
        }
    }
}