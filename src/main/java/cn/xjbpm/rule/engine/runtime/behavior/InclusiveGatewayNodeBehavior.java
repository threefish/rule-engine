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
import java.util.HashMap;
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
    public void doExecution(ExecutionEntity executionEntity, ExecutionScope executionScope) {
        ProcessRuntimeContext context = ProcessContextHolder.getContext();
        Map<String, Object> scopeNodeVariable = new HashMap<>();
        if (executionScope != null) {
            scopeNodeVariable = AdapterContextHolder.processVariableAdapter.findScopeNodeVariable(executionScope.getParentNodeExecutionId(), executionScope.getParentNodeKey());
        }
//        // 获取完成数，并加1，在计算范围内完成数1
//        Integer numberOfCompletedInstances=1;
//        AdapterContextHolder.processVariableAdapter.getCreateNodeVariable(executionEntity.getId(),context.getProcessIntanceId(), this.node.getKey(),
//                Collections.singletonMap(ProcessConstant.numberOfCompletedInstances, numberOfCompletedInstances));
//
//        Integer numberOfInstances = (Integer) scopeNodeVariable.get(ProcessConstant.numberOfInstances);

    }

    @Override
    public Node getCurrentNode() {
        return this.node;
    }


    @Override
    public void unableToComplete(ExecutionEntity executionEntity) {

    }
}
