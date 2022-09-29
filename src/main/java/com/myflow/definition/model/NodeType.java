package com.myflow.definition.model;

import com.myflow.definition.model.activity.FunctionActivityNode;
import com.myflow.definition.model.gateway.ExclusiveGatewayNode;
import com.myflow.definition.model.gateway.InclusiveGatewayNode;
import com.myflow.definition.model.gateway.ParallelGatewayNode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/29
 */
@AllArgsConstructor
@Getter
public enum NodeType {
    StartNode(StartNode.class),
    EndNode(EndNode.class),
    SequenceConnNode(SequenceConnNode.class),
    ExclusiveGatewayNode(ExclusiveGatewayNode.class),
    InclusiveGatewayNode(InclusiveGatewayNode.class),
    ParallelGatewayNode(ParallelGatewayNode.class),
    FunctionActivityNode(FunctionActivityNode.class);

    Class<?> clazz;

}
