package cn.xjbpm.rule.engine.runtime.behavior;

import cn.xjbpm.rule.engine.definition.model.gateway.InclusiveGatewayNode;
import cn.xjbpm.rule.engine.runtime.entity.ExecutionEntity;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/30
 */
public class InclusiveGatewayNodeBehavior implements NodeBehavior {

    private final InclusiveGatewayNode node;

    public InclusiveGatewayNodeBehavior(InclusiveGatewayNode node) {
        this.node = node;
    }

    @Override
    public void execution(ExecutionEntity executionEntity) {

    }

    @Override
    public void leave(ExecutionEntity executionEntity) {

    }
}
