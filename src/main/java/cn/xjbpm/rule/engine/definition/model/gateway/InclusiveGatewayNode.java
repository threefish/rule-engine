package cn.xjbpm.rule.engine.definition.model.gateway;

import cn.xjbpm.rule.engine.definition.model.NodeType;
import lombok.Data;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/29
 * 包容网关
 */
@Data
public class InclusiveGatewayNode extends GatewayNode {


    @Override
    public NodeType getType() {
        return NodeType.InclusiveGatewayNode;
    }
}
