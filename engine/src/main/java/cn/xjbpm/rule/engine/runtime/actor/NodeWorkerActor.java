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
import cn.xjbpm.rule.engine.definition.model.EndNode;
import cn.xjbpm.rule.engine.definition.model.Node;
import cn.xjbpm.rule.engine.definition.model.SequenceConnNode;
import cn.xjbpm.rule.engine.definition.model.StartNode;
import cn.xjbpm.rule.engine.definition.model.gateway.ExclusiveGatewayNode;
import cn.xjbpm.rule.engine.definition.model.gateway.InclusiveGatewayNode;
import cn.xjbpm.rule.engine.definition.model.gateway.ParallelGatewayNode;
import cn.xjbpm.rule.engine.runtime.behavior.NodeBehavior;
import cn.xjbpm.rule.engine.runtime.model.ExecutStatus;
import cn.xjbpm.rule.engine.runtime.model.FlowContext;
import cn.xjbpm.rule.engine.runtime.model.NodeExcution;
import lombok.extern.slf4j.Slf4j;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

/**
 * 池化版节点工作 Actor
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Slf4j
public class NodeWorkerActor extends AbstractActor {

    private final FlowContext flowContext;

    public NodeWorkerActor(FlowContext flowContext) {
        this.flowContext = flowContext;
    }

    public static Props props(FlowContext flowContext) {
        return Props.create(NodeWorkerActor.class, () -> new NodeWorkerActor(flowContext));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(WorkflowProtocol.ExecuteNode.class, this::handleExecute)
                .build();
    }

    private void handleExecute(WorkflowProtocol.ExecuteNode msg) {
        Node node = msg.getNode();
        try {
            if (!(node instanceof SequenceConnNode) && msg.getAttempt() > 0) {
                this.flowContext.addTraceLog(StringUtils.format("[{}] 开始第 {} 次重试执行", node.getName(), msg.getAttempt()));
            }

            NodeBehavior behavior = node.getBehavior();
            if (behavior != null) {
                behavior.execution(flowContext);
            } else {
                this.flowContext.addTraceLog(StringUtils.format("[{}] 未找到执行行为类 跳过执行", node.getId()));
            }

            long endTime = System.nanoTime();
            recordExecution(node, msg.getStartTotalTime(), endTime, ExecutStatus.SUCCESS, null);

            // 通知 Master 任务完成
            // 注意：在 Router 模式下，getSender() 依然是指向 Master (因为 Master 是通过 router.tell(msg, self) 发送的)
            getSender().tell(new WorkflowProtocol.NodeCompleted(node.getId(), node, true), getSelf());

        } catch (Exception e) {
            this.flowContext.addTraceLog(StringUtils.format("[{}] 执行异常: {}", node.getId(), e.getMessage()));
            handleFailure(msg, e);
        }
    }

    private void handleFailure(WorkflowProtocol.ExecuteNode msg, Exception e) {
        Node node = msg.getNode();
        int currentAttempt = msg.getAttempt();

        if (!isExcludeRetryNode(node)) {
            int maxRetries = getRetryCount(node);
            long delaySeconds = getRetryDelay(node);

            if (currentAttempt < maxRetries) {
                int nextAttempt = currentAttempt + 1;
                this.flowContext.addTraceLog(StringUtils.format("[{}] [{}] 启用重试机制: 最大重试{}次, 延迟 {}ms",
                        node.getId(), node.getName(), maxRetries, delaySeconds));
                this.flowContext.addTraceLog(StringUtils.format("[{}] 准备第{}次重试", node.getId(), nextAttempt));
                // 重试消息发给 Router (Parent)
                // getContext().parent() 在 Router 模式下指向的是 Router Actor
                // 这样重试任务会被重新负载均衡，不一定由当前 Worker 执行，效率更高
                getContext().system().scheduler().scheduleOnce(
                        FiniteDuration.create(delaySeconds, TimeUnit.MILLISECONDS),
                        getContext().parent(), // 发送给 Router
                        new WorkflowProtocol.ExecuteNode(node, nextAttempt, msg.getStartTotalTime()),
                        getContext().dispatcher(),
                        getSender() // Sender 保持为 Master
                );
                return;
            }
        }

        // 重试耗尽
        long endTime = System.nanoTime();
        this.flowContext.addTraceLog(StringUtils.format("[{}] 节点 最终失败 耗时{} 异常原因: {}",
                node.getId(), TimeFormatUtil.formatNanosToMs(endTime - msg.getStartTotalTime()), e.getMessage()));

        recordExecution(node, msg.getStartTotalTime(), endTime, ExecutStatus.FAILURE, e.getMessage());

        // 通知 Master 失败
        getSender().tell(new WorkflowProtocol.NodeFailed(node.getId(), node, e), getSelf());


    }

    private boolean isExcludeRetryNode(Node node) {
        return node instanceof StartNode
                || node instanceof SequenceConnNode
                || node instanceof InclusiveGatewayNode
                || node instanceof ExclusiveGatewayNode
                || node instanceof ParallelGatewayNode
                || node instanceof EndNode;
    }

    private int getRetryCount(Node node) {
        return node.getMaxRetries() == null ? 0 : node.getMaxRetries();
    }

    private long getRetryDelay(Node node) {
        return node.getRetryDelay() == null ? 0 : node.getRetryDelay();
    }

    private void recordExecution(Node node, long start, long end, ExecutStatus status, String error) {
        NodeExcution.NodeExcutionBuilder builder = NodeExcution.builder()
                .id(node.getId()).name(node.getName()).startTime(start).endTime(end)
                .status(status);
        if (error != null) {
            builder.errorMessage(error);
        }
        flowContext.putNodeExcution(node.getId(), builder.build());
    }
}