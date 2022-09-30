package com.myflow.runtime.behavior;

import com.myflow.definition.model.Node;
import com.myflow.definition.model.SequenceConnNode;
import com.myflow.runtime.entity.ExecutionEntity;
import com.myflow.runtime.util.ConditionUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/30
 */
@Slf4j
public class SequenceConnNodeBehavior implements NodeBehavior {

    private final SequenceConnNode node;

    public SequenceConnNodeBehavior(SequenceConnNode node) {
        this.node = node;
    }

    @Override
    public void execution(ExecutionEntity executionEntity) {
        log.info("[{}]执行处理逻辑", node.getKey());
        if (ConditionUtil.resolve(node.getConditionExpression(), executionEntity)) {
            this.leave(executionEntity);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("节点[{}]不满足通过条件", node.getKey());
            }
        }
    }


    @Override
    public void leave(ExecutionEntity executionEntity) {
        log.info("离开[{}]节点", node.getKey());
        Node targetNode = node.getTargetNode();
        targetNode.getBehavior().execution(executionEntity);
    }


}
