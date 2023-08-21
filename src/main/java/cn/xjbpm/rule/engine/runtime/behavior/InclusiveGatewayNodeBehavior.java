package cn.xjbpm.rule.engine.runtime.behavior;

import cn.xjbpm.rule.engine.adapter.AdapterContextHolder;
import cn.xjbpm.rule.engine.common.constant.ProcessConstant;
import cn.xjbpm.rule.engine.definition.model.Node;
import cn.xjbpm.rule.engine.definition.model.gateway.InclusiveGatewayNode;
import cn.xjbpm.rule.engine.runtime.context.ExecutionScope;
import cn.xjbpm.rule.engine.runtime.context.ProcessContextHolder;
import cn.xjbpm.rule.engine.runtime.context.ProcessRuntimeContext;
import cn.xjbpm.rule.engine.runtime.entity.ExecutionEntity;

import java.util.Collections;
import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/30
 */
public class InclusiveGatewayNodeBehavior extends BaseNodeBehavior {

    private final InclusiveGatewayNode node;

    public InclusiveGatewayNodeBehavior(InclusiveGatewayNode node) {
        this.node = node;
    }

    @Override
    public boolean doExecution(ExecutionEntity executionEntity, ExecutionScope executionScope) {
        ProcessRuntimeContext context = ProcessContextHolder.getContext();
        Map<String, Object> variable = context.getVariable();
        Map<String, Integer> scopeNodeVariable = (Map<String, Integer>) variable.getOrDefault(executionScope.getParentNodeKey(), Collections.EMPTY_MAP);
        int numberOfInstances = scopeNodeVariable.get(ProcessConstant.numberOfInstances);
        int numberOfCompletedInstances = scopeNodeVariable.get(ProcessConstant.numberOfCompletedInstances) + 1;
        scopeNodeVariable.put(ProcessConstant.numberOfCompletedInstances, numberOfCompletedInstances);
        AdapterContextHolder.processVariableAdapter.updateByProcessInstanceId(context.getProcessIntanceId(), variable);
        if (numberOfCompletedInstances >= numberOfInstances) {
            // 完成
            executionScope.setCleared(true);
            return true;
        } else {
            // 未完成
            return false;
        }
    }

    @Override
    public Node getCurrentNode() {
        return this.node;
    }


    @Override
    public void unableToComplete(ExecutionEntity executionEntity) {

    }
}
