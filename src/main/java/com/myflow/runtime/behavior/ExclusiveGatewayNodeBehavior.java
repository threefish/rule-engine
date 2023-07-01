package com.myflow.runtime.behavior;

import cn.hutool.core.util.StrUtil;
import com.myflow.aviator.AviatorContext;
import com.myflow.aviator.AviatorExecutor;
import com.myflow.definition.model.SequenceConnNode;
import com.myflow.definition.model.gateway.ExclusiveGatewayNode;
import com.myflow.runtime.entity.ExecutionEntity;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Comparator;
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
        Collections.sort(outgoingNodes, Comparator.comparing(SequenceConnNode::getSortNum));
        for (SequenceConnNode outgoingNode : outgoingNodes) {
            String expression = outgoingNode.getRule().getExpressionCacheString();
            AviatorContext aviatorContext = AviatorContext.create(expression, executionEntity.getVariable());
            if (StrUtil.isNotBlank(expression) || AviatorExecutor.executeBoolean(aviatorContext)) {
                outgoingNode.getBehavior().execution(executionEntity);
                // 独占网关，无需执行其他分支
                return;
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("节点[{}]不满足通过条件", node.getKey());
                }
            }
        }
        throw new RuntimeException(String.format("节点[%s:%s]无满足条件的分支！无法继续执行下去！", node.getName(), node.getKey()));
    }
}
