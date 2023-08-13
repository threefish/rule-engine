package cn.xjbpm.rule.engine.runtime.behavior;

import cn.xjbpm.rule.engine.definition.model.Node;
import cn.xjbpm.rule.engine.definition.model.SequenceConnNode;
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
    public void leave(ExecutionEntity executionEntity) {
        log.info("离开[{}]节点", node.getKey());
        Node targetNode = node.getTargetNode();
        targetNode.getBehavior().execution(executionEntity);
    }

    @Override
    public void unableToComplete(ExecutionEntity executionEntity) {

    }

    @Override
    public void doExecution(ExecutionEntity executionEntity) {
        log.info("[{}]执行处理逻辑", node.getKey());
    }

    @Override
    public Node getCurrentNode() {
        return node;
    }


}
