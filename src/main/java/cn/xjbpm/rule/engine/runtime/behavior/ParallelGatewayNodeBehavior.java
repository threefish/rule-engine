package cn.xjbpm.rule.engine.runtime.behavior;

import cn.xjbpm.rule.engine.adapter.AdapterContextHolder;
import cn.xjbpm.rule.engine.common.constant.ProcessConstant;
import cn.xjbpm.rule.engine.definition.model.Node;
import cn.xjbpm.rule.engine.definition.model.SequenceConnNode;
import cn.xjbpm.rule.engine.definition.model.gateway.ParallelGatewayNode;
import cn.xjbpm.rule.engine.runtime.context.ExecutionScope;
import cn.xjbpm.rule.engine.runtime.context.ProcessContextHolder;
import cn.xjbpm.rule.engine.runtime.context.ProcessRuntimeContext;
import cn.xjbpm.rule.engine.runtime.entity.ExecutionEntity;
import cn.xjbpm.rule.engine.runtime.util.ConditionUtil;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/30
 */
@Slf4j
public class ParallelGatewayNodeBehavior extends BaseNodeBehavior {

    private final ParallelGatewayNode node;

    public ParallelGatewayNodeBehavior(ParallelGatewayNode node) {
        this.node = node;
    }

    @Override
    public void leave(ExecutionEntity executionEntity) {
        this.leave(executionEntity, null);
    }

    @Override
    public void leave(ExecutionEntity executionEntity, ExecutionScope executionScope) {
        ProcessRuntimeContext context = ProcessContextHolder.getContext();
        Map<String, Object> variable = context.getVariable();
        int numberOfInstances = BigDecimal.ZERO.intValue();
        // 满足离开节点条件
        List<SequenceConnNode> outgoingNodes = node.getOutgoingNodes();
        Collections.sort(outgoingNodes, Comparator.comparing(SequenceConnNode::getSortNum));
        List<NodeBehavior> nodeBehaviors = new ArrayList<>();
        for (SequenceConnNode outgoingNode : outgoingNodes) {
            if (ConditionUtil.resolve(outgoingNode.getRule(), variable)) {
                numberOfInstances++;
                nodeBehaviors.add(outgoingNode.getBehavior());
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("节点[{}]不满足通过条件", node.getKey());
                }
            }
        }
        Map<String, Object> nodeVariable = new HashMap<>(2);
        nodeVariable.put(ProcessConstant.numberOfInstances, numberOfInstances);
        nodeVariable.put(ProcessConstant.numberOfCompletedInstances, BigDecimal.ZERO.intValue());

        variable.put(getCurrentNode().getKey(), nodeVariable);
        AdapterContextHolder.processVariableAdapter.updateByProcessInstanceId(context.getProcessIntanceId(), variable);
        nodeBehaviors.stream().forEach(behavior -> behavior.execution(executionEntity, createExecutionScope(executionEntity)));
        if (numberOfInstances == 0) {
            throw new RuntimeException(String.format("节点[%s:%s]无满足条件的分支！无法继续执行下去！", node.getName(), node.getKey()));
        }
        AdapterContextHolder.nodeExecutionAdapter.updateExecution2Completed(executionEntity);
    }

    @Override
    public boolean doExecution(ExecutionEntity executionEntity, ExecutionScope executionScope) {
        log.info("执行并行网关");
        return true;
    }

    @Override
    public Node getCurrentNode() {
        return this.node;
    }

    @Override
    public void unableToComplete(ExecutionEntity executionEntity) {

    }


    private ExecutionScope createExecutionScope(ExecutionEntity executionEntity) {
        return ExecutionScope.builder()
                .parentNodeExecutionId(executionEntity.getId())
                .parentNodeKey(executionEntity.getDefinitionKey())
                .build();
    }
}
