package com.myflow.definition.model.gateway;

import com.myflow.definition.model.NodeType;
import lombok.Data;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/29
 * 排他网关
 */
@Data
public class ExclusiveGatewayNode extends GatewayNode {

    @Override
    protected NodeType getType() {
        return NodeType.ExclusiveGatewayNode;
    }
}
