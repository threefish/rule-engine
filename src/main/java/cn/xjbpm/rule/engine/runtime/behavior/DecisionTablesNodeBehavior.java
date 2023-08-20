package cn.xjbpm.rule.engine.runtime.behavior;

import cn.hutool.core.collection.CollUtil;
import cn.xjbpm.rule.engine.common.utils.VariableUtils;
import cn.xjbpm.rule.engine.definition.model.Node;
import cn.xjbpm.rule.engine.definition.model.activity.DecisionTablesNode;
import cn.xjbpm.rule.engine.definition.model.activity.decisiontable.DecisionTablesRow;
import cn.xjbpm.rule.engine.definition.model.activity.decisiontable.DecisionTablesRowAssignColumn;
import cn.xjbpm.rule.engine.definition.model.activity.decisiontable.DecisionTablesRowConditionColumn;
import cn.xjbpm.rule.engine.runtime.context.ExecutionScope;
import cn.xjbpm.rule.engine.runtime.context.ProcessContextHolder;
import cn.xjbpm.rule.engine.runtime.context.ProcessRuntimeContext;
import cn.xjbpm.rule.engine.runtime.entity.ExecutionEntity;
import cn.xjbpm.rule.engine.runtime.util.ConditionUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/30
 */
@Slf4j
public class DecisionTablesNodeBehavior extends BaseNodeBehavior {

    private final DecisionTablesNode node;

    public DecisionTablesNodeBehavior(DecisionTablesNode node) {
        this.node = node;
    }

    @Override
    public void unableToComplete(ExecutionEntity executionEntity) {
        log.info("节点[{}]不满足完成条件，暂挂在此节点", node.getKey());
    }

    @Override
    public void doExecution(ExecutionEntity executionEntity, ExecutionScope executionScope) {
        ProcessRuntimeContext context = ProcessContextHolder.getContext();
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

    @Override
    public Node getCurrentNode() {
        return node;
    }


}
