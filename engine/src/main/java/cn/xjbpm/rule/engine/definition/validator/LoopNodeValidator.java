/*
 * Copyright 2025 threefish.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.xjbpm.rule.engine.definition.validator;

import cn.xjbpm.rule.common.utils.StringUtils;
import cn.xjbpm.rule.engine.definition.model.nodes.LoopNode;
import org.springframework.util.Assert;

/**
 * @author 黄川 huchuc@vip.qq.com
 */
public class LoopNodeValidator extends BaseNodeValidator<LoopNode> {

    @Override
    public void check(LoopNode node) {
        super.check(node);
        Assert.isTrue(StringUtils.isNotBlank(node.getCollectionVariableName()), "集合变量名不能为空");
//        Assert.isTrue(StringUtils.isNotBlank(node.getItemVariableName()), "单个元素的变量名不能为空");
    }
}