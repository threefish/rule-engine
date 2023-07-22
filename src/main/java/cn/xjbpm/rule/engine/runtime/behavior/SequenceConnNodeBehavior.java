package cn.xjbpm.rule.engine.runtime.behavior;

import cn.hutool.core.util.StrUtil;
import cn.xjbpm.rule.engine.aviator.AviatorContext;
import cn.xjbpm.rule.engine.aviator.AviatorExecutor;
import cn.xjbpm.rule.engine.definition.model.Node;
import cn.xjbpm.rule.engine.definition.model.SequenceConnNode;
import cn.xjbpm.rule.engine.runtime.entity.ExecutionEntity;
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
        if (this.node.getRule() == null) {
            this.leave(executionEntity);
        } else {
            String expression = this.node.getRule().getExpressionCacheString();
            AviatorContext aviatorContext = AviatorContext.create(expression, executionEntity.getVariable());
            if (StrUtil.isBlank(expression) || AviatorExecutor.executeBoolean(aviatorContext)) {
                this.leave(executionEntity);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("节点[{}]不满足通过条件", node.getKey());
                }
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
