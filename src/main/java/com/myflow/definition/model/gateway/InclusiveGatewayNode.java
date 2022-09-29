package com.myflow.definition.model.gateway;

import com.myflow.definition.model.NodeType;
import lombok.Data;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/29
 * 包容网关
 */
@Data
public class InclusiveGatewayNode extends GatewayNode {


    @Override
    protected NodeType getType() {
        return NodeType.InclusiveGatewayNode;
    }
}
