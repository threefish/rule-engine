package com.myflow.definition.model;

import com.myflow.definition.model.activity.DecisionTablesNode;
import com.myflow.definition.model.activity.FunctionActivityNode;
import com.myflow.definition.model.activity.RuleSetNode;
import com.myflow.definition.model.activity.ScoringCardNode;
import com.myflow.definition.model.gateway.ExclusiveGatewayNode;
import com.myflow.definition.model.gateway.InclusiveGatewayNode;
import com.myflow.definition.model.gateway.ParallelGatewayNode;
import com.myflow.definition.validator.*;
import com.myflow.runtime.behavior.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/29
 */
@AllArgsConstructor
@Getter
public enum NodeType {
    StartNode(StartNode.class, BaseNodeValidator.class, StartNodeBehavior.class),
    EndNode(EndNode.class, BaseNodeValidator.class, EndNodeBehavior.class),

    SequenceConnNode(SequenceConnNode.class, BaseNodeValidator.class, SequenceConnNodeBehavior.class),
    ExclusiveGatewayNode(ExclusiveGatewayNode.class, BaseNodeValidator.class, ExclusiveGatewayNodeBehavior.class),
    InclusiveGatewayNode(InclusiveGatewayNode.class, BaseNodeValidator.class, InclusiveGatewayNodeBehavior.class),
    ParallelGatewayNode(ParallelGatewayNode.class, BaseNodeValidator.class, ParallelGatewayNodeBehavior.class),
    FunctionActivityNode(FunctionActivityNode.class, FunctionActivityNodeValidator.class, FunctionActivityNodeBehavior.class),
    RuleSetNode(RuleSetNode.class, RuleSetNodeValidator.class, RuleSetNodeBehavior.class),

    ScoringCardNode(ScoringCardNode.class, ScoringCardNodeValidator.class, ScoringCardNodeBehavior.class),

    DecisionTablesNode(DecisionTablesNode.class, DecisionTablesNodeValidator.class, DecisionTablesNodeBehavior.class),


    ;

    Class<? extends Node> nodeClass;
    Class<? extends NodeValidator> validatorClass;
    Class<? extends NodeBehavior> behaviorClass;

}
