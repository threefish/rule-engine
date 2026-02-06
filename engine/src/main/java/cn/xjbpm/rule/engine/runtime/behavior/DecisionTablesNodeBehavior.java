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

import cn.hutool.core.collection.CollUtil;
import cn.xjbpm.rule.common.utils.ConditionUtil;
import cn.xjbpm.rule.common.utils.VariableUtils;
import cn.xjbpm.rule.engine.definition.model.nodes.DecisionTablesNode;
import cn.xjbpm.rule.engine.definition.model.nodes.decisiontable.DecisionTablesRow;
import cn.xjbpm.rule.engine.definition.model.nodes.decisiontable.DecisionTablesRowAssignColumn;
import cn.xjbpm.rule.engine.definition.model.nodes.decisiontable.DecisionTablesRowConditionColumn;
import cn.xjbpm.rule.engine.runtime.model.FlowContext;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/30
 */
@Slf4j
public class DecisionTablesNodeBehavior implements NodeBehavior {

    private final DecisionTablesNode node;

    public DecisionTablesNodeBehavior(DecisionTablesNode node) {
        this.node = node;
    }


    @Override
    public void execution(FlowContext context) {
        Map<String, Object> variable = context.getVariable();
        log.info("开始执行决策表");
        List<DecisionTablesRow> tableRows = this.node.getTableRows();
        if (CollUtil.isNotEmpty(tableRows)) {
            for (DecisionTablesRow tableRow : tableRows) {
                List<DecisionTablesRowConditionColumn> conditionColumns = tableRow.getConditionColumns();
                if (CollUtil.isNotEmpty(conditionColumns)) {
                    boolean status = conditionColumns.stream().allMatch(column -> ConditionUtil.resolve(column.getRule(), variable));
                    if (status) {
                        List<DecisionTablesRowAssignColumn> assignColumns = tableRow.getAssignColumns();
                        if (CollUtil.isNotEmpty(assignColumns)) {
                            for (DecisionTablesRowAssignColumn assignColumn : assignColumns) {
                                VariableUtils.setPathVariable(assignColumn.getName(), assignColumn.getExpression(), assignColumn.getExpression(), variable);
                            }
                        }
                    }
                }
            }
        }
    }
}