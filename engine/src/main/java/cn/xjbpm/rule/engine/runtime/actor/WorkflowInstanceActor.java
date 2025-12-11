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
import cn.xjbpm.rule.common.utils.ConditionUtil;
import cn.xjbpm.rule.common.utils.StringUtils;
import cn.xjbpm.rule.engine.definition.model.EndNode;
import cn.xjbpm.rule.engine.definition.model.Node;
import cn.xjbpm.rule.engine.definition.model.SequenceConnNode;
import cn.xjbpm.rule.engine.definition.model.gateway.ExclusiveGatewayNode;
import cn.xjbpm.rule.engine.definition.model.gateway.InclusiveGatewayNode;
import cn.xjbpm.rule.engine.definition.model.gateway.ParallelGatewayNode;
import cn.xjbpm.rule.engine.runtime.model.ExecutStatus;
import cn.xjbpm.rule.engine.runtime.model.FlowContext;
import cn.xjbpm.rule.engine.runtime.model.NodeExcution;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 流程实例 Actor：负责调度整个流程
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Slf4j
public class WorkflowInstanceActor extends AbstractActor {

    private final Map<String, List<Node>> nodeNextMap;
    private final Map<String, Integer> convergePendingCount;
    // 记录被“激活”的汇聚网关ID（即至少接收到一个 Active 信号的网关）
    private final Set<String> activatedGateways = new HashSet<>();
    // 路由池引用
    private final ActorRef workerRouter;
    // 跳过时用
    private Set<String> skipNodeIds;
    private FlowContext flowContext;
    private ActorRef caller;
    // 当前正在执行 (发送给 Worker) 的节点数
    private int runningNodesCount = 0;

    public WorkflowInstanceActor(NodeDependencyBuilder dependencyBuilder, ActorRef globalWorkerRouter) {
        this.nodeNextMap = dependencyBuilder.getNodeNextMap();
        this.convergePendingCount = new ConcurrentHashMap<>(dependencyBuilder.getConvergePendingCount());
        this.workerRouter = globalWorkerRouter;
    }

    public static Props props(NodeDependencyBuilder dependencyBuilder, ActorRef globalWorkerRouter) {
        return Props.create(WorkflowInstanceActor.class, () -> new WorkflowInstanceActor(dependencyBuilder, globalWorkerRouter));
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
        this.caller = getSender();
        this.skipNodeIds = new HashSet<>(msg.getSkipNodeIds());
        Node startNode = msg.getRuleFlowModel().getStartNode();
        log.info("流程启动: {}", startNode.getId());
        this.flowContext.addTraceLog(StringUtils.format("[{}] 流程启动", startNode.getId()));
        scheduleNodeExecution(startNode);
    }

    private void handleNodeCompleted(WorkflowProtocol.NodeCompleted msg) {
        Node currentNode = msg.getNode();
        // 无论是来自 Worker 还是来自自身的跳过消息，都视为任务完成，减少计数器
        runningNodesCount--;
        if (currentNode instanceof EndNode) {
            this.flowContext.addTraceLog(StringUtils.format("[{}] 结束节点,执行完成,流程分支结束", currentNode.getId()));
        } else {
            scheduleNextNodes(currentNode);
        }
        checkGlobalTermination();
    }

    private void handleNodeFailed(WorkflowProtocol.NodeFailed msg) {
        Node failedNode = msg.getNode();
        // 失败的节点必然是 Worker 正在处理的节点，因此需要减计数
        runningNodesCount--;
        log.error("执行失败 节点: {} 正在执行节点数:{} 原因:{}", failedNode.getId(), runningNodesCount, msg.getReason());
        this.flowContext.addTraceLog(StringUtils.format("[{}] 流程异常中断: {}", failedNode.getId(), msg.getReason().getMessage()));

        // 异常中断，直接通知调用者失败并停止流程
        if (caller != null) {
            caller.tell(new Status.Failure(msg.getReason()), getSelf());
        }
        getContext().stop(getSelf());
    }

    /**
     * 检查当前是否有正在执行的节点，如果没有，则终止整个流程。
     */
    private void checkGlobalTermination() {
        if (runningNodesCount == 0) {
            this.flowContext.addTraceLog("所有节点执行完毕");
            if (caller != null) {
                caller.tell(new Status.Success(null), getSelf());
            }
            // 停止 Master Actor，同时停止 Worker Router
            getContext().stop(getSelf());
        }
    }

    private void scheduleNextNodes(Node currentNode) {
        List<Node> nextNodes = nodeNextMap.getOrDefault(currentNode.getId(), new ArrayList<>());
        if (nextNodes.isEmpty()) {
            this.flowContext.addTraceLog(StringUtils.format("[{}] 无后续节点 流程分支结束", currentNode.getId()));
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
        if (targetNode instanceof ExclusiveGatewayNode) {
            handleExclusiveGateway((ExclusiveGatewayNode) targetNode);
            return;
        }
        if (targetNode instanceof ParallelGatewayNode) {
            this.flowContext.addTraceLog(StringUtils.format("[{}] 并行网关触发后续调度", targetNode.getId()));
            recordVirtualExecution(targetNode, ExecutStatus.SUCCESS);
            scheduleNextNodes(targetNode);
            return;
        }
        if (targetNode instanceof InclusiveGatewayNode) {
            // dispatchNode 代表正常执行流，标记为 activePath = true
            handleInclusiveGateway((InclusiveGatewayNode) targetNode, true);
            return;
        }
        scheduleNodeExecution(targetNode);
    }

    private void handleExclusiveGateway(ExclusiveGatewayNode gateway) {
        List<SequenceConnNode> nextNodes = nodeNextMap.getOrDefault(gateway.getId(), new ArrayList<>())
                .stream()
                .filter(node -> node instanceof SequenceConnNode)
                .map(node -> (SequenceConnNode) node)
                .sorted(Comparator.comparingInt(SequenceConnNode::getSortNum))
                .collect(Collectors.toList());
        SequenceConnNode selectedNode = null;
        // 1. 寻找第一个满足条件的连接线
        for (SequenceConnNode connector : nextNodes) {
            boolean isMatch;
            if (connector.getRule() == null || StringUtils.isBlank(connector.getRule().getExpressionCacheString())) {
                isMatch = true;
            } else {
                String expr = connector.getRule().getExpressionCacheString();
                try {
                    isMatch = ConditionUtil.resolve(expr, flowContext.getVariable());
                } catch (Exception e) {
                    this.flowContext.addTraceLog(StringUtils.format("[{}] 排他网关条件计算异常: {}", connector.getId(), e.getMessage()));
                    isMatch = false;
                }
            }
            if (isMatch) {
                selectedNode = connector;
                break; // 找到第一个即停止，体现排他性
            }
        }
        // 2. 执行选中的节点，跳过其他节点
        boolean hasExecuted = false;
        for (SequenceConnNode connector : nextNodes) {
            if (connector == selectedNode) {
                this.flowContext.addTraceLog(StringUtils.format("[{}] 排他网关命中节点: {}", gateway.getId(), connector.getId()));
                recordVirtualExecution(connector, ExecutStatus.SUCCESS, true);
                scheduleNextNodes(connector);
                hasExecuted = true;
            } else {
                // 未被选中的路径，标记为条件不满足并传播跳过，确保下游汇聚节点能正确计数
                recordVirtualExecution(connector, ExecutStatus.SUCCESS, false);
                propagateSkippedPath(connector);
            }
        }

        recordVirtualExecution(gateway, hasExecuted ? ExecutStatus.SUCCESS : ExecutStatus.FAILURE);
        if (!hasExecuted) {
            this.flowContext.addTraceLog(StringUtils.format("[{}] 排他网关无符合条件路径", gateway.getId()));
        }
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

    /**
     * 处理汇聚网关
     *
     * @param gateway    汇聚网关节点
     * @param activePath 是否来自有效执行路径（true=正常执行, false=路径跳过传播）
     */
    private void handleInclusiveGateway(InclusiveGatewayNode gateway, boolean activePath) {
        String key = gateway.getId();
        // 如果是有效路径到达，记录该网关已被激活
        if (activePath) {
            activatedGateways.add(key);
        }
        Integer pending = convergePendingCount.computeIfPresent(key, (k, v) -> v - 1);
        if (pending != null) {
            if (activePath) {
                this.flowContext.addTraceLog(StringUtils.format("[{}] 聚合节点,剩余待完成分支:{}", gateway.getId(), pending));
            }
            // 只有当所有分支（无论是执行还是跳过）都到达时，才进行下一步判断
            if (pending <= 0) {
                // 只有当网关被“激活”（至少有一个 Active 分支）时，才真正执行
                if (activatedGateways.contains(key)) {
                    this.flowContext.addTraceLog(StringUtils.format("[{}] 聚合节点,并行分支完成,继续执行", gateway.getId()));
                    recordVirtualExecution(gateway, ExecutStatus.SUCCESS);
                    scheduleNodeExecution(gateway);
                } else {
                    log.debug(StringUtils.format("[{}] 聚合节点所有前置分支均被跳过,该节点自动跳过", gateway.getId()));
                    // 记录为跳过
                    recordVirtualExecution(gateway, ExecutStatus.SKIP, false);
                    propagateSkippedPath(gateway);
                }
                // 清理状态
                activatedGateways.remove(key);
            } else {
                // 等待其他分支
                recordVirtualExecution(gateway, ExecutStatus.WAITING);
            }
        }
    }

    private void propagateSkippedPath(Node node) {
        List<Node> nextNodes = nodeNextMap.getOrDefault(node.getId(), new ArrayList<>());
        for (Node next : nextNodes) {
            if (next instanceof InclusiveGatewayNode) {
                // propagateSkippedPath 代表路径跳过，标记 activePath = false
                handleInclusiveGateway((InclusiveGatewayNode) next, false);
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
        // 统一处理计数器：无论是跳过还是提交给 Worker，都视为一个需要等待完成的任务
        runningNodesCount++;
        // 检查是否需要跳过
        if (this.skipNodeIds.contains(node.getId())) {
            log.info("节点 [{}] 处于跳过执行列表中，跳过实际执行，自动流转。", node.getId());
            this.flowContext.addTraceLog(StringUtils.format("[{}] 指定跳过节点,跳过执行", node.getId()));
            // 记录一条虚拟的执行记录，标记为 SUCCESS
            recordVirtualExecution(node, ExecutStatus.SUCCESS);
            // 触发完成从而驱动流程继续向下 (Sender is self)
            getSelf().tell(new WorkflowProtocol.NodeCompleted(node.getId(), node, true), getSelf());
            return;
        }
        // 路由池会自动选择一个空闲的 Worker (或者轮询) 来处理
        workerRouter.tell(new WorkflowProtocol.ExecuteNode(node, 0, System.nanoTime(), flowContext), getSelf());
    }
}