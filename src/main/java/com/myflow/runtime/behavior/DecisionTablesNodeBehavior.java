package com.myflow.runtime.behavior;

import com.myflow.definition.model.Node;
import com.myflow.definition.model.activity.DecisionTablesNode;
import com.myflow.runtime.entity.ExecutionEntity;
import lombok.extern.slf4j.Slf4j;

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
    public void doExecution(ExecutionEntity executionEntity) {
        log.info("开始执行决策表");
    }

    @Override
    public Node getNode() {
        return node;
    }


}
