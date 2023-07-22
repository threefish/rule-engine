package cn.xjbpm.rule.engine.definition.model;

import cn.xjbpm.rule.engine.definition.model.activity.FunctionActivityNode;
import cn.xjbpm.rule.engine.definition.model.gateway.ExclusiveGatewayNode;
import cn.xjbpm.rule.engine.definition.model.gateway.InclusiveGatewayNode;
import cn.xjbpm.rule.engine.definition.model.gateway.ParallelGatewayNode;
import cn.xjbpm.rule.engine.definition.validator.*;
import cn.xjbpm.rule.engine.runtime.behavior.*;
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
    RuleSetNode(cn.xjbpm.rule.engine.definition.model.activity.RuleSetNode.class, RuleSetNodeValidator.class, RuleSetNodeBehavior.class),

    ScoringCardNode(cn.xjbpm.rule.engine.definition.model.activity.ScoringCardNode.class, ScoringCardNodeValidator.class, ScoringCardNodeBehavior.class),

    DecisionTablesNode(cn.xjbpm.rule.engine.definition.model.activity.DecisionTablesNode.class, DecisionTablesNodeValidator.class, DecisionTablesNodeBehavior.class),


    ;

    Class<? extends Node> nodeClass;
    Class<? extends NodeValidator> validatorClass;
    Class<? extends NodeBehavior> behaviorClass;

}
