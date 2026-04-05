/**
 * Copyright 2025 threefish.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
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
    AssignmentNode(AssignmentNode.class, AssignmentNodeValidator.class, AssignmentNodeBehavior.class),
    ExclusiveGatewayNode(ExclusiveGatewayNode.class, BaseNodeValidator.class, ExclusiveGatewayNodeBehavior.class),
    InclusiveGatewayNode(InclusiveGatewayNode.class, BaseNodeValidator.class, InclusiveGatewayNodeBehavior.class),
    ParallelGatewayNode(ParallelGatewayNode.class, BaseNodeValidator.class, ParallelGatewayNodeBehavior.class),
    FunctionNode(FunctionNode.class, FunctionNodeValidator.class, FunctionNodeBehavior.class),
    RuleSetNode(RuleSetNode.class, RuleSetNodeValidator.class, RuleSetNodeBehavior.class),
    ScoringCardNode(ScoringCardNode.class, ScoringCardNodeValidator.class, ScoringCardNodeBehavior.class),
    DecisionTablesNode(DecisionTablesNode.class, DecisionTablesNodeValidator.class, DecisionTablesNodeBehavior.class),
    DmnDecisionTableNode(DmnDecisionTableNode.class, DmnDecisionTableNodeValidator.class, DmnDecisionTableNodeBehavior.class),
    DelayWaitNode(DelayWaitNode.class, DelayWaitNodeValidator.class, DelayWaitNodeBehavior.class),
    HttpNode(HttpNode.class, HttpNodeValidator.class, HttpNodeBehavior.class),
    ShellNode(ShellNode.class, ShellNodeValidator.class, ShellNodeBehavior.class),
    SshNode(SshNode.class, SshNodeValidator.class, SshNodeBehavior.class),
    DownloadFileNode(DownloadFileNode.class, DownloadFileNodeValidator.class, DownloadFileNodeBehavior.class),
    LoopNode(LoopNode.class, LoopNodeValidator.class, LoopNodeBehavior.class),
    AITextNode(AITextNode.class, AITextNodeValidator.class, AITextNodeBehavior.class),
    AIImageNode(AIImageNode.class, AIImageNodeValidator.class, AIImageNodeBehavior.class),
    AITTSNode(AITTSNode.class, AITTSNodeValidator.class, AITTSNodeBehavior.class),
    DBNode(DBNode.class, DBNodeValidator.class, DBNodeBehavior.class),
    FTPNode(FTPNode.class, FTPNodeValidator.class, FTPNodeBehavior.class),
    EmailNode(EmailNode.class, EmailNodeValidator.class, EmailNodeBehavior.class),
    ExcelReadNode(ExcelReadNode.class, ExcelReadNodeValidator.class, ExcelReadNodeBehavior.class),
    ExcelWriteNode(ExcelWriteNode.class, ExcelWriteNodeValidator.class, ExcelWriteNodeBehavior.class),
    CSVNode(CSVNode.class, CSVNodeValidator.class, CSVNodeBehavior.class),
    RedisNode(RedisNode.class, RedisNodeValidator.class, RedisNodeBehavior.class),
    RabbitMQNode(RabbitMQNode.class, RabbitMQNodeValidator.class, RabbitMQNodeBehavior.class),
    KafkaNode(KafkaNode.class, KafkaNodeValidator.class, KafkaNodeBehavior.class),
    MQTTNode(MQTTNode.class, MQTTNodeValidator.class, MQTTNodeBehavior.class),
    DingTalkNode(DingTalkNode.class, DingTalkNodeValidator.class, DingTalkNodeBehavior.class),
    FeishuNode(FeishuNode.class, FeishuNodeValidator.class, FeishuNodeBehavior.class),
    WordNode(WordNode.class, WordNodeValidator.class, WordNodeBehavior.class),
    PdfNode(PdfNode.class, PdfNodeValidator.class, PdfNodeBehavior.class),
    CryptoNode(CryptoNode.class, CryptoNodeValidator.class, CryptoNodeBehavior.class),
    OcrNode(OcrNode.class, OcrNodeValidator.class, OcrNodeBehavior.class),
    RocketMQNode(RocketMQNode.class, RocketMQNodeValidator.class, RocketMQNodeBehavior.class),
    WxPayNode(WxPayNode.class, WxPayNodeValidator.class, WxPayNodeBehavior.class),
    GitNode(GitNode.class, GitNodeValidator.class, GitNodeBehavior.class),
    MavenNode(MavenNode.class, MavenNodeValidator.class, MavenNodeBehavior.class),
    ;

    Class<? extends Node> nodeClass;
    Class<? extends NodeValidator> validatorClass;
    Class<? extends NodeBehavior> behaviorClass;

}