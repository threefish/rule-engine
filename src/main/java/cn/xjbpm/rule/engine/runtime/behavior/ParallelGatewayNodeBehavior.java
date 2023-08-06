package cn.xjbpm.rule.engine.runtime.behavior;

import cn.hutool.core.util.StrUtil;
import cn.xjbpm.rule.engine.aviator.AviatorContext;
import cn.xjbpm.rule.engine.aviator.AviatorExecutor;
import cn.xjbpm.rule.engine.definition.model.SequenceConnNode;
import cn.xjbpm.rule.engine.definition.model.gateway.ParallelGatewayNode;
import cn.xjbpm.rule.engine.runtime.entity.ExecutionEntity;
import cn.xjbpm.rule.engine.runtime.util.ConditionUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/30
 */
@Slf4j
public class ParallelGatewayNodeBehavior implements NodeBehavior {

    private final ParallelGatewayNode node;

    public ParallelGatewayNodeBehavior(ParallelGatewayNode node) {
        this.node = node;
    }

    @Override
    public void execution(ExecutionEntity executionEntity) {
        log.info("执行并行网关");
        this.leave(executionEntity);
    }

    @Override
    public void leave(ExecutionEntity executionEntity) {
        int i = 0;
        // 满足离开节点条件
        List<SequenceConnNode> outgoingNodes = node.getOutgoingNodes();
        Collections.sort(outgoingNodes, Comparator.comparing(SequenceConnNode::getSortNum));
        for (SequenceConnNode outgoingNode : outgoingNodes) {
            String expression = outgoingNode.getRule().getExpressionCacheString();
            if (StrUtil.isNotBlank(expression) || ConditionUtil.resolve(expression, executionEntity.getVariable())) {
                i++;
                outgoingNode.getBehavior().execution(executionEntity);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("节点[{}]不满足通过条件", node.getKey());
                }
            }
        }
        if (i == 0) {
            throw new RuntimeException(String.format("节点[%s:%s]无满足条件的分支！无法继续执行下去！", node.getName(), node.getKey()));
        }
    }
}
