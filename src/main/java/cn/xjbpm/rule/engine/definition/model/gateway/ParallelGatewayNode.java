package cn.xjbpm.rule.engine.definition.model.gateway;

import cn.xjbpm.rule.engine.definition.model.NodeType;
import lombok.Data;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/29
 * 并行网关
 */
@Data
public class ParallelGatewayNode extends GatewayNode {

    @Override
    public NodeType getType() {
        return NodeType.ParallelGatewayNode;
    }
}
