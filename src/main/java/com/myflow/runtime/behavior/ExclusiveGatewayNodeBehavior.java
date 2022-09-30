package com.myflow.runtime.behavior;

import cn.hutool.core.util.StrUtil;
import com.myflow.definition.model.SequenceConnNode;
import com.myflow.definition.model.gateway.ExclusiveGatewayNode;
import com.myflow.runtime.entity.ExecutionEntity;
import com.myflow.runtime.util.ConditionUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/30
 */
@Slf4j
public class ExclusiveGatewayNodeBehavior implements NodeBehavior {

    private final ExclusiveGatewayNode node;

    public ExclusiveGatewayNodeBehavior(ExclusiveGatewayNode node) {
        this.node = node;
    }

    @Override
    public void execution(ExecutionEntity executionEntity) {
        log.info("执行排他网关");
        this.leave(executionEntity);
    }

    @Override
    public void leave(ExecutionEntity executionEntity) {
        // 满足离开节点条件
        List<SequenceConnNode> outgoingNodes = node.getOutgoingNodes();
        for (SequenceConnNode outgoingNode : outgoingNodes) {
            if (StrUtil.isNotBlank(outgoingNode.getConditionExpression())) {
                if (ConditionUtil.resolve(outgoingNode.getConditionExpression(), executionEntity)) {
                    outgoingNode.getBehavior().execution(executionEntity);
                    // 独占网关，无需执行其他分支
                    return;
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("节点[{}]不满足通过条件", node.getKey());
                    }
                }
            } else {
                outgoingNode.getBehavior().execution(executionEntity);
                // 独占网关，无需执行其他分支
                return;
            }
        }
    }
}
