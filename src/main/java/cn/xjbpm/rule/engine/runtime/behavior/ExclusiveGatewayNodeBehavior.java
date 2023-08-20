package cn.xjbpm.rule.engine.runtime.behavior;

import cn.hutool.core.util.StrUtil;
import cn.xjbpm.rule.engine.definition.model.Node;
import cn.xjbpm.rule.engine.definition.model.SequenceConnNode;
import cn.xjbpm.rule.engine.definition.model.gateway.ExclusiveGatewayNode;
import cn.xjbpm.rule.engine.runtime.context.ExecutionScope;
import cn.xjbpm.rule.engine.runtime.context.ProcessContextHolder;
import cn.xjbpm.rule.engine.runtime.context.ProcessRuntimeContext;
import cn.xjbpm.rule.engine.runtime.entity.ExecutionEntity;
import cn.xjbpm.rule.engine.runtime.util.ConditionUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/30
 */
@Slf4j
public class ExclusiveGatewayNodeBehavior extends BaseNodeBehavior {

    private final ExclusiveGatewayNode node;

    public ExclusiveGatewayNodeBehavior(ExclusiveGatewayNode node) {
        this.node = node;
    }


    @Override
    public void unableToComplete(ExecutionEntity executionEntity) {

    }

    @Override
    public void doExecution(ExecutionEntity executionEntity, ExecutionScope executionScope) {
        log.info("执行排他网关");
        this.leave(executionEntity,null);
    }

    @Override
    public void leave(ExecutionEntity executionEntity, ExecutionScope executionScope) {
        ProcessRuntimeContext context = ProcessContextHolder.getContext();
        Map<String, Object> variable = context.getVariable();
        // 满足离开节点条件
        List<SequenceConnNode> outgoingNodes = node.getOutgoingNodes();
        Collections.sort(outgoingNodes, Comparator.comparing(SequenceConnNode::getSortNum));
        for (SequenceConnNode outgoingNode : outgoingNodes) {
            String expression = outgoingNode.getRule().getExpressionCacheString();
            if (StrUtil.isNotBlank(expression) || ConditionUtil.resolve(expression, variable)) {
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


    @Override
    public Node getCurrentNode() {
        return this.node;
    }
}
