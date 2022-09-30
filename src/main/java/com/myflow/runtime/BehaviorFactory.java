package com.myflow.runtime;

import com.myflow.definition.model.*;
import com.myflow.definition.model.activity.FunctionActivityNode;
import com.myflow.definition.model.gateway.ExclusiveGatewayNode;
import com.myflow.definition.model.gateway.InclusiveGatewayNode;
import com.myflow.definition.model.gateway.ParallelGatewayNode;
import com.myflow.runtime.behavior.*;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/30
 */
public class BehaviorFactory {
    public static NodeBehavior createBehavior(NodeType type, Node childNode) {
        NodeBehavior behavior;
        switch (type) {
            case StartNode:
                behavior = new StartNodeBehavior((StartNode) childNode);
                break;
            case EndNode:
                behavior = new EndNodeBehavior((EndNode) childNode);
                break;
            case SequenceConnNode:
                behavior = new SequenceConnNodeBehavior((SequenceConnNode) childNode);
                break;
            case ExclusiveGatewayNode:
                behavior = new ExclusiveGatewayNodeBehavior((ExclusiveGatewayNode) childNode);
                break;
            case InclusiveGatewayNode:
                behavior = new InclusiveGatewayNodeBehavior((InclusiveGatewayNode) childNode);
                break;
            case ParallelGatewayNode:
                behavior = new ParallelGatewayNodeBehavior((ParallelGatewayNode) childNode);
                break;
            case FunctionActivityNode:
                behavior = new FunctionActivityNodeBehavior((FunctionActivityNode) childNode);
                break;
            default:
                behavior = null;
        }
        return behavior;
    }
}
