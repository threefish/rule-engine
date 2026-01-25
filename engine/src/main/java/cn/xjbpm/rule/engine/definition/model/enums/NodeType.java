/*
 * Copyright 2025 threefish.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.xjbpm.rule.engine.definition.model.enums;

import cn.xjbpm.rule.engine.definition.model.nodes.*;
import cn.xjbpm.rule.engine.definition.model.nodes.gateway.ExclusiveGatewayNode;
import cn.xjbpm.rule.engine.definition.model.nodes.gateway.InclusiveGatewayNode;
import cn.xjbpm.rule.engine.definition.model.nodes.gateway.ParallelGatewayNode;
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
    FunctionNode(FunctionNode.class, FunctionNodeValidator.class, FunctionNodeBehavior.class),
    RuleSetNode(RuleSetNode.class, RuleSetNodeValidator.class, RuleSetNodeBehavior.class),
    ScoringCardNode(ScoringCardNode.class, ScoringCardNodeValidator.class, ScoringCardNodeBehavior.class),
    DecisionTablesNode(DecisionTablesNode.class, DecisionTablesNodeValidator.class, DecisionTablesNodeBehavior.class),
    DelayWaitNode(DelayWaitNode.class, DelayWaitNodeValidator.class, DelayWaitNodeBehavior.class),
    HttpNode(HttpNode.class, HttpNodeValidator.class, HttpNodeBehavior.class),
    ShellNode(ShellNode.class, ShellNodeValidator.class, ShellNodeBehavior.class),
    SshNode(SshNode.class, SshNodeValidator.class, SshNodeBehavior.class),
    VolcanoImageGenerationNode(VolcanoImageGenerationNode.class, VolcanoImageGenerationNodeValidator.class, VolcanoImageGenerationNodeBehavior.class),
    VolcanoChatGenerationNode(VolcanoChatGenerationNode.class, VolcanoChatGenerationNodeValidator.class, VolcanoChatGenerationNodeBehavior.class),
    DownloadFileNode(DownloadFileNode.class, DownloadFileNodeValidator.class, DownloadFileNodeBehavior.class),
    LoopNode(LoopNode.class, LoopNodeValidator.class, LoopNodeBehavior.class),
    ;

    Class<? extends Node> nodeClass;
    Class<? extends NodeValidator> validatorClass;
    Class<? extends NodeBehavior> behaviorClass;

}