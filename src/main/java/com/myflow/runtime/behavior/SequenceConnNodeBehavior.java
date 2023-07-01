package com.myflow.runtime.behavior;

import cn.hutool.core.util.StrUtil;
import com.myflow.aviator.AviatorContext;
import com.myflow.aviator.AviatorExecutor;
import com.myflow.definition.model.Node;
import com.myflow.definition.model.SequenceConnNode;
import com.myflow.rule.translate.RuleExpressionTranslate;
import com.myflow.runtime.entity.ExecutionEntity;
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
        this.node.setConditionalExpression(new RuleExpressionTranslate(node.getRule()).getExpression());
    }

    @Override
    public void execution(ExecutionEntity executionEntity) {
        log.info("[{}]执行处理逻辑", node.getKey());
        if (this.node.getRule() == null) {
            this.leave(executionEntity);
        } else {
            String expression = this.node.getConditionalExpression();
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
