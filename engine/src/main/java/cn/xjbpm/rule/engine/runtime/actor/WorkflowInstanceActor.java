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
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Status;
import akka.routing.RoundRobinPool;
import cn.xjbpm.rule.common.utils.ConditionUtil;
import cn.xjbpm.rule.common.utils.StringUtils;
import cn.xjbpm.rule.engine.definition.model.EndNode;
import cn.xjbpm.rule.engine.definition.model.Node;
import cn.xjbpm.rule.engine.definition.model.SequenceConnNode;
import cn.xjbpm.rule.engine.definition.model.gateway.InclusiveGatewayNode;
import cn.xjbpm.rule.engine.definition.model.gateway.ParallelGatewayNode;
import cn.xjbpm.rule.engine.runtime.model.ExecutStatus;
import cn.xjbpm.rule.engine.runtime.model.FlowContext;
import cn.xjbpm.rule.engine.runtime.model.NodeExcution;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 流程实例 Actor：负责调度整个流程
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Slf4j
public class WorkflowInstanceActor extends AbstractActor {

    /**
     * 根据并发度调整
     */
    private static final int WORKER_POOL_SIZE = 30;
    private final Map<String, List<Node>> nodeNextMap;
    private final Map<String, Integer> convergePendingCount;
    // 路由池引用
    private ActorRef workerRouter;
    private FlowContext flowContext;
    private ActorRef originalRequester;

    public WorkflowInstanceActor(NodeDependencyBuilder dependencyBuilder) {
        this.nodeNextMap = dependencyBuilder.getNodeNextMap();
        this.convergePendingCount = new ConcurrentHashMap<>(dependencyBuilder.getConvergePendingCount());
    }

    public static Props props(NodeDependencyBuilder dependencyBuilder) {
        return Props.create(WorkflowInstanceActor.class, () -> new WorkflowInstanceActor(dependencyBuilder));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(WorkflowProtocol.StartProcess.class, this::handleStart)
                .match(WorkflowProtocol.NodeCompleted.class, this::handleNodeCompleted)
                .match(WorkflowProtocol.NodeFailed.class, this::handleNodeFailed)
                .build();
    }

    private void handleStart(WorkflowProtocol.StartProcess msg) {
        this.flowContext = msg.getFlowContext();
        this.originalRequester = getSender();

        // 初始化 Worker Pool  使用 RoundRobinPool 创建一组复用的 Worker
        // 这些 Worker 共享同一个 FlowContext (这是安全的，因为 FlowContext 是线程安全的且属于同一个流程实例)
        this.workerRouter = getContext().actorOf(
                new RoundRobinPool(WORKER_POOL_SIZE)
                        .props(NodeWorkerActor.props(flowContext)),
                "worker-router"
        );

        Node startNode = msg.getProcessModel().getStartNode();
        log.info("流程启动: {}", startNode.getName());
        this.flowContext.addTraceLog(StringUtils.format("[{}] 流程启动", startNode.getName()));
        scheduleNodeExecution(startNode);
    }

    private void handleNodeCompleted(WorkflowProtocol.NodeCompleted msg) {
        Node currentNode = msg.getNode();
        if (currentNode instanceof EndNode) {
            handleEndNode(currentNode);
            return;
        }
        scheduleNextNodes(currentNode);
    }

    private void handleEndNode(Node endNode) {
        this.flowContext.addTraceLog(StringUtils.format("[{}] 结束节点 执行完成 流程分支结束", endNode.getId()));
        if (originalRequester != null) {
            originalRequester.tell(new Status.Success(null), getSelf());
        }
        // 停止 Master 会自动停止所有的 Child (包括 Router 和 Workers)
        getContext().stop(getSelf());
    }

    private void handleNodeFailed(WorkflowProtocol.NodeFailed msg) {
        log.error("流程异常中断，节点: {}", msg.getNode().getName(), msg.getReason());
        this.flowContext.addTraceLog(StringUtils.format("[{}] 流程异常中断: {}", msg.getNode().getId(), msg.getReason().getMessage()));
        if (originalRequester != null) {
            originalRequester.tell(new Status.Failure(msg.getReason()), getSelf());
        }
        getContext().stop(getSelf());
    }

    private void scheduleNextNodes(Node currentNode) {
        List<Node> nextNodes = nodeNextMap.getOrDefault(currentNode.getId(), new ArrayList<>());
        if (nextNodes.isEmpty()) {
            this.flowContext.addTraceLog(StringUtils.format("[{}] 无后续节点 流程分支结束", currentNode.getName()));
            return;
        }
        for (Node nextNode : nextNodes) {
            dispatchNode(nextNode);
        }
    }

    private void dispatchNode(Node targetNode) {
        if (targetNode instanceof SequenceConnNode) {
            handleSequenceConnector((SequenceConnNode) targetNode);
            return;
        }
        if (targetNode instanceof ParallelGatewayNode) {
            this.flowContext.addTraceLog(StringUtils.format("[{}] 并行网关触发后续调度", targetNode.getName()));
            recordVirtualExecution(targetNode, ExecutStatus.SUCCESS);
            scheduleNextNodes(targetNode);
            return;
        }
        if (targetNode instanceof InclusiveGatewayNode) {
            handleInclusiveGateway((InclusiveGatewayNode) targetNode);
            return;
        }
        scheduleNodeExecution(targetNode);
    }

    private void handleSequenceConnector(SequenceConnNode connector) {
        boolean shouldExecute = true;
        String exprLog = "无条件";
        if (connector.getRule() != null) {
            String expr = connector.getRule().getExpressionCacheString();
            exprLog = expr;
            try {
                shouldExecute = ConditionUtil.resolve(expr, flowContext.getVariable());
            } catch (Exception e) {
                this.flowContext.addTraceLog(StringUtils.format("[{}] 条件计算异常: {}", connector.getId(), e.getMessage()));
                shouldExecute = false;
            }
        }
        recordVirtualExecution(connector, ExecutStatus.SUCCESS, shouldExecute);
        if (shouldExecute) {
            if (connector.getRule() != null) {
                this.flowContext.addTraceLog(StringUtils.format("[{}] 条件表达式:\"{}\" 计算结果:true", connector.getId(), exprLog));
            }
            scheduleNextNodes(connector);
        } else {
            this.flowContext.addTraceLog(StringUtils.format("[{}] 条件表达式:\"{}\" 计算结果:false", connector.getId(), exprLog));
            propagateSkippedPath(connector);
        }
    }

    private void handleInclusiveGateway(InclusiveGatewayNode gateway) {
        String key = gateway.getId();
        Integer pending = convergePendingCount.computeIfPresent(key, (k, v) -> v - 1);
        if (pending != null) {
            this.flowContext.addTraceLog(StringUtils.format("[{}] 聚合节点,剩余待完成分支:{}", gateway.getName(), pending));
            recordVirtualExecution(gateway, ExecutStatus.WAITING);
            if (pending <= 0) {
                this.flowContext.addTraceLog(StringUtils.format("[{}] 聚合节点,并行分支完成", gateway.getName()));
                recordVirtualExecution(gateway, ExecutStatus.SUCCESS);
                scheduleNodeExecution(gateway);
            }
        }
    }

    private void propagateSkippedPath(Node node) {
        List<Node> nextNodes = nodeNextMap.getOrDefault(node.getId(), new ArrayList<>());
        for (Node next : nextNodes) {
            if (next instanceof InclusiveGatewayNode) {
                this.flowContext.addTraceLog(StringUtils.format("[{}] 跳过路径更新汇聚节点计数", next.getId()));
                handleInclusiveGateway((InclusiveGatewayNode) next);
                return;
            }
            propagateSkippedPath(next);
        }
    }

    private void recordVirtualExecution(Node node, ExecutStatus status) {
        recordVirtualExecution(node, status, null);
    }

    private void recordVirtualExecution(Node node, ExecutStatus status, Boolean condition) {
        long now = System.nanoTime();
        NodeExcution.NodeExcutionBuilder builder = NodeExcution.builder()
                .id(node.getId()).name(node.getName()).startTime(now).endTime(now).status(status);
        if (condition != null) {
            builder.conditionsMeet(condition);
        }
        flowContext.putNodeExcution(node.getId(), builder.build());
    }

    private void scheduleNodeExecution(Node node) {
        // 不再 actorOf 创建新 Actor，而是直接 tell 路由池
        // 路由池会自动选择一个空闲的 Worker (或者轮询) 来处理
        workerRouter.tell(new WorkflowProtocol.ExecuteNode(node, 0, System.nanoTime()), getSelf());
    }
}