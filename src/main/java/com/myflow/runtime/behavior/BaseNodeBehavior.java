package com.myflow.runtime.behavior;

import cn.hutool.core.util.StrUtil;
import com.myflow.definition.model.Node;
import com.myflow.definition.model.SequenceConnNode;
import com.myflow.runtime.entity.ExecutionEntity;
import com.myflow.runtime.util.ConditionUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/30
 */
@Slf4j
public abstract class BaseNodeBehavior implements NodeBehavior {


    @Override
    public void execution(ExecutionEntity executionEntity) {
        ExecutionEntity currentExecutionEntity = executionEntity.createChild();
        Node node = getNode();
        log.info("执行 节点ID:{} TAG:{} 名称:{}", node.getKey(), node.getTag(), node.getName());
        if (StrUtil.isNotBlank(node.getSkipExpression())) {
            // 跳过表达式不为空，表示需要进行判断
            if (ConditionUtil.resolve(node.getSkipExpression(), currentExecutionEntity)) {
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
            if (ConditionUtil.resolve(getNode().getCompletionExpression(), currentExecutionEntity)) {
                this.leave(currentExecutionEntity);
            } else {
                this.unableToComplete(currentExecutionEntity);
            }
        }
    }

    @Override
    public void leave(ExecutionEntity executionEntity) {
        log.info("离开[{}:{}]节点", getNode().getName(), getNode().getKey());
        // 满足离开节点条件
        List<SequenceConnNode> outgoingNodes = getNode().getOutgoingNodes();
        for (SequenceConnNode outgoingNode : outgoingNodes) {
            outgoingNode.getBehavior().execution(executionEntity);
        }
    }

    /**
     * 不满足完成条件
     * 需要进行再次处理，是否需要重试，是否延迟重试等
     */
    public abstract void unableToComplete(ExecutionEntity executionEntity);

    /**
     * 执行
     *
     * @param executionEntity
     */
    public abstract void doExecution(ExecutionEntity executionEntity);

    public abstract Node getNode();

}
