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

package cn.xjbpm.rule.engine.runtime.actor;

import akka.actor.AbstractActor;
import akka.actor.Props;
import cn.xjbpm.rule.common.utils.StringUtils;
import cn.xjbpm.rule.common.utils.TimeFormatUtil;
import cn.xjbpm.rule.engine.definition.model.enums.ErrorStrategy;
import cn.xjbpm.rule.engine.definition.model.nodes.*;
import cn.xjbpm.rule.engine.definition.model.nodes.gateway.ExclusiveGatewayNode;
import cn.xjbpm.rule.engine.definition.model.nodes.gateway.InclusiveGatewayNode;
import cn.xjbpm.rule.engine.definition.model.nodes.gateway.ParallelGatewayNode;
import cn.xjbpm.rule.engine.runtime.behavior.NodeBehavior;
import cn.xjbpm.rule.engine.runtime.model.ExecutStatus;
import cn.xjbpm.rule.engine.runtime.model.FlowContext;
import cn.xjbpm.rule.engine.runtime.model.NodeExcution;
import cn.xjbpm.rule.event.RuleFlowDebugEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import scala.concurrent.duration.Duration;
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
                .match(InternalDelayFinish.class, this::onDelayFinished)
                .build();
    }

    private void handleExecute(WorkflowProtocol.ExecuteNode msg) {
        Node node = msg.getNode();
        FlowContext flowContext = msg.getFlowContext(); // 从消息中获取上下文

        try {
            if (!(node instanceof SequenceConnNode) && msg.getAttempt() > 0) {
                flowContext.addTraceLog(node.getId(), "开始第 {} 次重试执行", new Object[]{msg.getAttempt()});
                if (log.isDebugEnabled()) {
                    log.debug("[{}] 开始第 {} 次重试执行", node.getId(), msg.getAttempt());
                }
            }

            recordExecution(flowContext, node, msg.getStartTotalTime(), msg.getStartTotalTime(), ExecutStatus.RUNNING, null);
            if (node instanceof DelayWaitNode) {
                long delayTime = ((DelayWaitNode) node).getDelayTime();
                log.info("节点 [{}] 进入延迟等待: {}ms", node.getId(), delayTime);
                flowContext.addTraceLog(node.getId(),"进入延迟等待: {}ms",  delayTime);
                // N秒后发送一条特殊的“延迟完成”消息给自己或 Master
                getContext().getSystem().scheduler().scheduleOnce(
                        Duration.create(delayTime, TimeUnit.MILLISECONDS),
                        getSelf(),
                        new InternalDelayFinish(msg, getSender()),
                        getContext().getDispatcher(),
                        getSender()
                );
            } else {
                performWork(msg);
            }
        } catch (Exception e) {
            handleFailure(msg, flowContext, e);
        }
    }

    private void performWork(WorkflowProtocol.ExecuteNode msg) throws Exception {
        Node node = msg.getNode();
        FlowContext flowContext = msg.getFlowContext(); // 从消息中获取上下文
        NodeBehavior behavior = node.getBehavior();
        long startTime = System.nanoTime();
        if (msg.getNode() instanceof DelayWaitNode) {
            startTime = msg.getStartTotalTime();
        }
        if (behavior != null) {
            behavior.execution(flowContext);
        } else {
            flowContext.addTraceLog(node.getId(), "未找到执行行为类 跳过执行");
            if (log.isWarnEnabled()) {
                log.warn("[{}] 未找到执行行为类 跳过执行", node.getId());
            }
        }

        long endTime = System.nanoTime();
        recordExecution(flowContext, node, startTime, endTime, ExecutStatus.SUCCESS, null);

        // 通知 Master 任务完成
        getSender().tell(new WorkflowProtocol.NodeCompleted(node.getId(), node, true, msg.getScope(), startTime), getSelf());
    }

    /**
     * 内部消息：延迟时间到
     */
    private void onDelayFinished(InternalDelayFinish finishMsg) throws Exception {
        log.info("节点 [{}] 延迟结束，继续执行", finishMsg.getMsg().getNode().getId());
        DelayWaitNode node = (DelayWaitNode) finishMsg.getMsg().getNode();
        FlowContext flowContext = finishMsg.getMsg().getFlowContext();
        flowContext.addTraceLog(node.getId(),"延迟{}ms结束，继续执行",new Object[]{node.getDelayTime()});
        performWork(finishMsg.getMsg());
    }

    private void handleFailure(WorkflowProtocol.ExecuteNode msg, FlowContext flowContext, Exception e) {
        Node node = msg.getNode();
        int currentAttempt = msg.getAttempt();
        if (isExcludeRetryNode(node) || !node.isRetryOnFail()) {
            flowContext.addTraceLog(node.getId(),"执行异常: {}",new Object[]{e.getMessage()});
            if (log.isDebugEnabled()) {
                log.debug("[{}] 执行异常: {}", node.getId(), e.getMessage());
            }
            recordExecution(flowContext, node, msg.getStartTotalTime(), System.nanoTime(), ExecutStatus.FAILURE, e.getMessage());
        } else {
            int maxRetries = node.getMaxRetries();
            long delaySeconds = node.getRetryDelay();
            if (currentAttempt < maxRetries) {
                int nextAttempt = currentAttempt + 1;
                flowContext.addTraceLog(node.getId(),"执行异常 启用重试 准备第{}次重试 最大重试{}次 延迟{}ms",
                        new Object[]{nextAttempt, maxRetries, delaySeconds}
                );
                if (log.isDebugEnabled()) {
                    log.debug("[{}] 执行异常 启用重试 准备第{}次重试 最大重试{}次 延迟{}ms");
                }
                // 重试时，务必将 flowContext 继续传递下去
                getContext().system().scheduler().scheduleOnce(
                        FiniteDuration.create(delaySeconds, TimeUnit.MILLISECONDS),
                        getContext().parent(), // 发送给 Router (实际上是 Global Router)
                        new WorkflowProtocol.ExecuteNode(node, nextAttempt, msg.getStartTotalTime(), flowContext, msg.getScope()),
                        getContext().dispatcher(),
                        getSender() // Sender 保持为 Master (WorkflowInstanceActor)
                );
                return;
            }
            // 重试耗尽
            long endTime = System.nanoTime();
            if (log.isDebugEnabled()) {
                log.debug("[{}] 重试次数耗尽 依然执行失败 异常描述: {}", node.getId(), e);
            }
            flowContext.addTraceLog(node.getId(),"执行失败 耗时{} 异常描述: {}",new Object[]{TimeFormatUtil.formatNanosToMs(endTime - msg.getStartTotalTime()), e.getMessage()});
            recordExecution(flowContext, node, msg.getStartTotalTime(), endTime, ExecutStatus.FAILURE, e.getMessage());
        }

        if (getErrorStrategy(node) == ErrorStrategy.TERMINATE) {
            getSender().tell(new WorkflowProtocol.NodeFailed(node.getId(), node, e), getSelf());
        } else {
            getSender().tell(new WorkflowProtocol.NodeCompleted(node.getId(), node, false, msg.getScope(), msg.getStartTotalTime()), getSelf());
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

    private void recordExecution(FlowContext flowContext, Node node, long start, long end, ExecutStatus status, String error) {
        NodeExcution.NodeExcutionBuilder builder = NodeExcution.builder()
                .id(node.getId()).name(node.getName()).startTime(start).endTime(end)
                .nodeType(node.getType())
                .status(status);
        if (error != null) {
            builder.errorMessage(error);
        }
        flowContext.putNodeExcution(node.getId(), builder.build());
        if (flowContext.isDebugModel()) {
            ApplicationEventPublisher publishManager = flowContext.getBeanContextManager().getEventPublishManager();
            if (Objects.nonNull(publishManager)) {
                publishManager.publishEvent(RuleFlowDebugEvent.create(flowContext));
            }
        }
    }

    private static class InternalDelayFinish {
        private final WorkflowProtocol.ExecuteNode msg;
        private final akka.actor.ActorRef master; // 需要记住 Master 是谁，否则延迟回来不知道报给谁

        public InternalDelayFinish(WorkflowProtocol.ExecuteNode msg, akka.actor.ActorRef master) {
            this.msg = msg;
            this.master = master;
        }

        public WorkflowProtocol.ExecuteNode getMsg() {
            return msg;
        }

        public akka.actor.ActorRef getMaster() {
            return master;
        }
    }
}