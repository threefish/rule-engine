package cn.xjbpm.rule.engine.runtime.behavior;

import cn.xjbpm.rule.engine.adapter.AdapterContextHolder;
import cn.xjbpm.rule.engine.definition.model.Node;
import cn.xjbpm.rule.engine.definition.model.SequenceConnNode;
import cn.xjbpm.rule.engine.runtime.context.ExecutionScope;
import cn.xjbpm.rule.engine.runtime.entity.ExecutionEntity;
import lombok.extern.slf4j.Slf4j;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/30
 */
@Slf4j
public class SequenceConnNodeBehavior extends BaseNodeBehavior {

    private final SequenceConnNode node;

    public SequenceConnNodeBehavior(SequenceConnNode node) {
        this.node = node;
    }


    @Override
    public void leave(ExecutionEntity executionEntity, ExecutionScope executionScope) {
        log.info("离开 {}:{}:{} 节点",getCurrentNode().getType(), getCurrentNode().getName(), getCurrentNode().getKey());
        Node targetNode = node.getTargetNode();
        targetNode.getBehavior().execution(executionEntity,executionScope);
    }

    @Override
    public void unableToComplete(ExecutionEntity executionEntity) {

    }

    @Override
    public boolean doExecution(ExecutionEntity executionEntity, ExecutionScope executionScope) {
        log.info("[{}]执行处理逻辑", node.getKey());
        AdapterContextHolder.nodeExecutionAdapter.updateExecution2Completed(executionEntity);
        return true;
    }

    @Override
    public Node getCurrentNode() {
        return node;
    }


}
