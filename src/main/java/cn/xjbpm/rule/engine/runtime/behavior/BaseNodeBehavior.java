package cn.xjbpm.rule.engine.runtime.behavior;

import cn.hutool.core.util.StrUtil;
import cn.xjbpm.rule.engine.adapter.AdapterContextHolder;
import cn.xjbpm.rule.engine.adapter.persistence.po.NodeExecutionEntity;
import cn.xjbpm.rule.engine.definition.model.Node;
import cn.xjbpm.rule.engine.definition.model.SequenceConnNode;
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
public abstract class BaseNodeBehavior implements NodeBehavior {


    protected ExecutionEntity createChild(ExecutionEntity parentEntity) {
        NodeExecutionEntity executionEntity = new NodeExecutionEntity();
        executionEntity.setParentId(parentEntity.getId());
        executionEntity.setProcessInstanceId(parentEntity.getProcessInstanceId());
        executionEntity.setDefinitionName(this.getCurrentNode().getName());
        executionEntity.setNodeType(this.getCurrentNode().getType());
        executionEntity.setDefinitionKey(this.getCurrentNode().getKey());
        executionEntity.setCompleted(false);
        executionEntity.setActive(true);
        AdapterContextHolder.nodeExecutionAdapter.createExecutionEntity(executionEntity);
        return executionEntity;
    }

    @Override
    public void execution(ExecutionEntity executionEntity) {
        ExecutionEntity currentExecutionEntity = createChild(executionEntity);
        ProcessRuntimeContext context = ProcessContextHolder.getContext();
        Map<String, Object> variable = context.getVariable();
        Node node = getCurrentNode();
        log.info("执行 节点ID:{} TAG:{} 名称:{}", node.getKey(), node.getTag(), node.getName());
        if (StrUtil.isNotBlank(node.getSkipExpression())) {
            // 跳过表达式不为空，表示需要进行判断
            if (ConditionUtil.resolve(node.getSkipExpression(), variable)) {
                // 表示当前节点满足跳过规则，执行跳过
                if (log.isDebugEnabled()) {
                    log.debug("节点[{}]满足跳过条件", node.getKey());
                }
                this.leave(currentExecutionEntity);
            } else {
                this.doExecution(currentExecutionEntity);
            }
        } else {
            //执行
            this.doExecution(currentExecutionEntity);
            // 执行后判断是否满足完成条件，为空表示则任务满足
            if (ConditionUtil.resolve(getCurrentNode().getCompletionExpression(), variable)) {
                this.leave(currentExecutionEntity);
            } else {
                this.unableToComplete(currentExecutionEntity);
            }
        }
    }

    @Override
    public void leave(ExecutionEntity executionEntity) {
        log.info("离开[{}:{}]节点", getCurrentNode().getName(), getCurrentNode().getKey());
        // 满足离开节点条件
        ProcessRuntimeContext context = ProcessContextHolder.getContext();
        Map<String, Object> variable = context.getVariable();
        List<SequenceConnNode> outgoingNodes = getCurrentNode().getOutgoingNodes();
        for (SequenceConnNode outgoingNode : outgoingNodes) {
            if (ConditionUtil.resolve(outgoingNode.getRule(), variable)) {
                outgoingNode.getBehavior().execution(executionEntity);
            }
        }
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
    public abstract void doExecution(ExecutionEntity executionEntity);

    /**
     * 取得节点
     *
     * @return
     */
    public abstract Node getCurrentNode();

}
