package cn.xjbpm.rule.engine.runtime.behavior;

import cn.hutool.core.util.StrUtil;
import cn.xjbpm.rule.engine.adapter.AdapterContextHolder;
import cn.xjbpm.rule.engine.adapter.persistence.po.NodeExecutionEntity;
import cn.xjbpm.rule.engine.definition.model.Node;
import cn.xjbpm.rule.engine.definition.model.SequenceConnNode;
import cn.xjbpm.rule.engine.runtime.context.ExecutionScope;
import cn.xjbpm.rule.engine.runtime.context.ProcessContextHolder;
import cn.xjbpm.rule.engine.runtime.context.ProcessRuntimeContext;
import cn.xjbpm.rule.engine.runtime.entity.ExecutionEntity;
import cn.xjbpm.rule.engine.runtime.util.ConditionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/30
 */
@Slf4j
public abstract class BaseNodeBehavior implements NodeBehavior {

    @Override
    public void execution(ExecutionEntity executionEntity) {
        this.execution(executionEntity, null);
    }


    @Override
    public void execution(ExecutionEntity executionEntity, ExecutionScope executionScope) {
        ExecutionEntity currentExecutionEntity = createOrFindExecution(executionEntity, executionScope);
        ProcessRuntimeContext context = ProcessContextHolder.getContext();
        Map<String, Object> variable = context.getVariable();
        Node node = getCurrentNode();
        log.info("执行 节点ID:{} TAG:{} 名称:{}", node.getKey(), node.getTag(), node.getName());
        if (StrUtil.isNotBlank(node.getSkipExpression()) && ConditionUtil.resolve(node.getSkipExpression(), variable)) {
            // 跳过表达式不为空， 表示当前节点满足跳过规则，执行跳过
            if (log.isDebugEnabled()) {
                log.debug("节点[{}]满足跳过条件", node.getKey());
            }
            this.leave(currentExecutionEntity, executionScope);
        } else {
            //执行
            if (this.doExecution(currentExecutionEntity, executionScope)) {
                if (Objects.nonNull(executionScope) && executionScope.isCleared()) {
                    executionScope = executionScope.getParentExecutionScope();
                }
                // 执行后判断是否满足完成条件，为空表示则任务满足
                if (ConditionUtil.resolve(getCurrentNode().getCompletionExpression(), variable)) {
                    this.leave(currentExecutionEntity, executionScope);
                } else {
                    this.unableToComplete(currentExecutionEntity);
                }
            } else {
                this.unableToComplete(currentExecutionEntity);
            }
        }
    }


    @Override
    public void leave(ExecutionEntity executionEntity) {
        this.leave(executionEntity, null);
    }

    @Override
    public void leave(ExecutionEntity executionEntity, ExecutionScope executionScope) {
        AdapterContextHolder.nodeExecutionAdapter.updateExecution2Completed(executionEntity);
        log.info("离开 {}:{}:{} 节点", getCurrentNode().getType(), getCurrentNode().getName(), getCurrentNode().getKey());
        // 满足离开节点条件
        ProcessRuntimeContext context = ProcessContextHolder.getContext();
        Map<String, Object> variable = context.getVariable();
        List<SequenceConnNode> outgoingNodes = getCurrentNode().getOutgoingNodes();
        List<NodeBehavior> behaviors = new ArrayList<>();
        for (SequenceConnNode outgoingNode : outgoingNodes) {
            if (ConditionUtil.resolve(outgoingNode.getRule(), variable)) {
                behaviors.add(outgoingNode.getBehavior());
            }
        }
        Assert.notEmpty(behaviors, String.format("没有继续运行下去的路径!当前节点名:%s 节点key:%s", getCurrentNode().getName(), getCurrentNode().getKey()));
        behaviors.stream().forEach(behavior -> behavior.execution(executionEntity, executionScope));
    }

    protected ExecutionEntity createOrFindExecution(ExecutionEntity parentEntity, ExecutionScope executionScope) {
        NodeExecutionEntity notCompletedExecution = AdapterContextHolder.nodeExecutionAdapter
                .findNotCompletedExecution(parentEntity.getProcessInstanceId(), this.getCurrentNode().getKey());
        if (notCompletedExecution != null) {
            return notCompletedExecution;
        }
        NodeExecutionEntity executionEntity = new NodeExecutionEntity();
        if (executionScope != null) {
            executionEntity.setParentId(executionScope.getParentNodeExecutionId());
            executionEntity.setScopeNodeKey(executionScope.getParentNodeKey());
        }
        executionEntity.setProcessInstanceId(parentEntity.getProcessInstanceId());
        executionEntity.setDefinitionName(this.getCurrentNode().getName());
        executionEntity.setNodeType(this.getCurrentNode().getType());
        executionEntity.setDefinitionKey(this.getCurrentNode().getKey());
        executionEntity.setCompleted(false);
        executionEntity.setActive(true);
        AdapterContextHolder.nodeExecutionAdapter.createExecutionEntity(executionEntity);
        return executionEntity;
    }

    /**
     * 不满足完成条件
     * 需要进行再次处理，是否需要重试，是否延迟重试等
     *
     * @param executionEntity
     */
    public abstract void unableToComplete(ExecutionEntity executionEntity);

    /**
     * 执行
     *
     * @param executionEntity
     */
    public abstract boolean doExecution(ExecutionEntity executionEntity, ExecutionScope executionScope);

    /**
     * 取得节点
     *
     * @return
     */
    public abstract Node getCurrentNode();

}
