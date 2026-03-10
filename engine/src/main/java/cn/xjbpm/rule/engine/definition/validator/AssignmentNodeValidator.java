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
import cn.xjbpm.rule.engine.definition.model.nodes.AssignmentNode;
import org.springframework.util.Assert;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/30
 */
public class AssignmentNodeValidator implements NodeValidator<AssignmentNode> {

    @Override
    public void check(AssignmentNode assignmentNode) throws Exception {
        if (CollUtil.isNotEmpty(assignmentNode.getLocalAssignments())) {
            for (AssignmentNode.Assignment assignment : assignmentNode.getLocalAssignments()) {
                Assert.notNull(assignment, "临时赋值字段和临时赋值表达式不能为空");
                Assert.isTrue(StringUtils.isNotBlank(assignment.getField()), "临时赋值字段不能为空");
                Assert.isTrue(StringUtils.isNotBlank(assignment.getExpression()), "临时赋值表达式不能为空");
            }
        }
        Assert.isTrue(CollUtil.isNotEmpty(assignmentNode.getAssignments()), "赋值列表不能为空");
        for (AssignmentNode.Assignment assignment : assignmentNode.getAssignments()) {
            Assert.notNull(assignment, "赋值字段和赋值表达式不能为空");
            Assert.isTrue(StringUtils.isNotBlank(assignment.getField()), "赋值字段不能为空");
            Assert.isTrue(StringUtils.isNotBlank(assignment.getExpression()), "赋值表达式不能为空");
        }
    }
}