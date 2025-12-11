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
package cn.xjbpm.rule.engine.runtime.actor;

import akka.actor.AbstractActor;
import akka.actor.Props;
import cn.xjbpm.rule.common.utils.StringUtils;
import cn.xjbpm.rule.common.utils.TimeFormatUtil;
import cn.xjbpm.rule.engine.definition.model.*;
import cn.xjbpm.rule.engine.definition.model.enums.ErrorStrategy;
import cn.xjbpm.rule.engine.definition.model.gateway.ExclusiveGatewayNode;
import cn.xjbpm.rule.engine.definition.model.gateway.InclusiveGatewayNode;
import cn.xjbpm.rule.engine.definition.model.gateway.ParallelGatewayNode;
import cn.xjbpm.rule.engine.runtime.behavior.NodeBehavior;
import cn.xjbpm.rule.engine.runtime.model.ExecutStatus;
import cn.xjbpm.rule.engine.runtime.model.FlowContext;
import cn.xjbpm.rule.engine.runtime.model.NodeExcution;
import lombok.extern.slf4j.Slf4j;
import scala.concurrent.duration.FiniteDuration;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 全局共享节点工作 Actor (无状态)
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Slf4j
public class NodeWorkerActor extends AbstractActor {


    public static Props props() {
        return Props.create(NodeWorkerActor.class, NodeWorkerActor::new);
    }

    public NodeWorkerActor() {
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(WorkflowProtocol.ExecuteNode.class, this::handleExecute)
                .build();
    }

    private void handleExecute(WorkflowProtocol.ExecuteNode msg) {
        Node node = msg.getNode();
        FlowContext flowContext = msg.getFlowContext(); // 从消息中获取上下文

        try {
            if (!(node instanceof SequenceConnNode) && msg.getAttempt() > 0) {
                flowContext.addTraceLog(StringUtils.format("[{}] 开始第 {} 次重试执行", node.getId(), msg.getAttempt()));
            }

            NodeBehavior behavior = node.getBehavior();
            if (behavior != null) {
                behavior.execution(flowContext);
            } else {
                flowContext.addTraceLog(StringUtils.format("[{}] 未找到执行行为类 跳过执行", node.getId()));
            }

            long endTime = System.nanoTime();
            recordExecution(flowContext, node, msg.getStartTotalTime(), endTime, ExecutStatus.SUCCESS, null);

            // 通知 Master 任务完成
            getSender().tell(new WorkflowProtocol.NodeCompleted(node.getId(), node, true), getSelf());

        } catch (Exception e) {
            handleFailure(msg, flowContext, e);
        }
    }

    private void handleFailure(WorkflowProtocol.ExecuteNode msg, FlowContext flowContext, Exception e) {
        Node node = msg.getNode();
        int currentAttempt = msg.getAttempt();
        if (isExcludeRetryNode(node) || !node.isRetryOnFail()) {
            flowContext.addTraceLog(StringUtils.format("[{}] 执行异常: {}", node.getId(), e.getMessage()));
            recordExecution(flowContext, node, msg.getStartTotalTime(), System.nanoTime(), ExecutStatus.FAILURE, e.getMessage());
        } else {
            int maxRetries = node.getMaxRetries();
            long delaySeconds = node.getRetryDelay();
            if (currentAttempt < maxRetries) {
                int nextAttempt = currentAttempt + 1;
                flowContext.addTraceLog(StringUtils.format("[{}] 执行异常 启用重试 准备第{}次重试 最大重试{}次 延迟{}ms",
                        node.getId(), nextAttempt, maxRetries, delaySeconds));

                // 重试时，务必将 flowContext 继续传递下去
                getContext().system().scheduler().scheduleOnce(
                        FiniteDuration.create(delaySeconds, TimeUnit.MILLISECONDS),
                        getContext().parent(), // 发送给 Router (实际上是 Global Router)
                        new WorkflowProtocol.ExecuteNode(node, nextAttempt, msg.getStartTotalTime(), flowContext),
                        getContext().dispatcher(),
                        getSender() // Sender 保持为 Master (WorkflowInstanceActor)
                );
                return;
            }
            // 重试耗尽
            long endTime = System.nanoTime();
            flowContext.addTraceLog(StringUtils.format("[{}] 执行失败 耗时{} 异常描述: {}",
                    node.getId(), TimeFormatUtil.formatNanosToMs(endTime - msg.getStartTotalTime()), e.getMessage()));
            recordExecution(flowContext, node, msg.getStartTotalTime(), endTime, ExecutStatus.FAILURE, e.getMessage());
        }

        if (getErrorStrategy(node) == ErrorStrategy.TERMINATE) {
            getSender().tell(new WorkflowProtocol.NodeFailed(node.getId(), node, e), getSelf());
        } else {
            getSender().tell(new WorkflowProtocol.NodeCompleted(node.getId(), node, false), getSelf());
        }
    }

    private boolean isExcludeRetryNode(Node node) {
        return node instanceof StartNode
                || node instanceof SequenceConnNode
                || node instanceof InclusiveGatewayNode
                || node instanceof ExclusiveGatewayNode
                || node instanceof ParallelGatewayNode
                || node instanceof DelayWaitNode
                || node instanceof EndNode;
    }

    private ErrorStrategy getErrorStrategy(Node node) {
        return Objects.nonNull(node.getErrorStrategy()) ? node.getErrorStrategy() : ErrorStrategy.TERMINATE;
    }

    private void recordExecution(FlowContext ctx, Node node, long start, long end, ExecutStatus status, String error) {
        NodeExcution.NodeExcutionBuilder builder = NodeExcution.builder()
                .id(node.getId()).name(node.getName()).startTime(start).endTime(end)
                .status(status);
        if (error != null) {
            builder.errorMessage(error);
        }
        ctx.putNodeExcution(node.getId(), builder.build());
    }
}