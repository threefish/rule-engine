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
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Status;
import cn.xjbpm.rule.common.utils.ConditionUtil;
import cn.xjbpm.rule.common.utils.StringUtils;
import cn.xjbpm.rule.common.utils.VariableUtils;
import cn.xjbpm.rule.engine.definition.model.nodes.EndNode;
import cn.xjbpm.rule.engine.definition.model.nodes.LoopNode;
import cn.xjbpm.rule.engine.definition.model.nodes.Node;
import cn.xjbpm.rule.engine.definition.model.nodes.SequenceConnNode;
import cn.xjbpm.rule.engine.definition.model.nodes.gateway.ExclusiveGatewayNode;
import cn.xjbpm.rule.engine.definition.model.nodes.gateway.InclusiveGatewayNode;
import cn.xjbpm.rule.engine.definition.model.nodes.gateway.ParallelGatewayNode;
import cn.xjbpm.rule.engine.runtime.model.ExecutStatus;
import cn.xjbpm.rule.engine.runtime.model.FlowContext;
import cn.xjbpm.rule.engine.runtime.model.NodeExcution;
import lombok.extern.slf4j.Slf4j;
import scala.concurrent.duration.Duration;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 规则流实例 Actor：负责调度整个规则流
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
    // Key: LoopNodeId, Value: 该循环下正在运行的子任务数
    private final Map<String, Integer> loopActiveChildCount = new HashMap<>();
    // 缓存 LoopNode 实例，以便在循环结束时能找到它触发后续
    private final Map<String, LoopNode> activeLoopNodes = new HashMap<>();
    // 循环迭代状态：Key: LoopNodeId, Value: 迭代状态
    private final Map<String, LoopIterationState> loopIterationStates = new HashMap<>();
    /**
     * 调试事件发布回调，由 service 层注入，引擎本身不感知 Spring。
     */
    private final Consumer<FlowContext> debugPublisher;
    // 跳过时用
    private Set<String> skipNodeIds;
    private FlowContext flowContext;
    private ActorRef caller;
    // 当前正在执行 (发送给 Worker) 的节点数
    private int runningNodesCount = 0;

    public WorkflowInstanceActor(NodeDependencyBuilder dependencyBuilder, ActorRef globalWorkerRouter,
                                 Consumer<FlowContext> debugPublisher) {
        this.nodeNextMap = dependencyBuilder.getNodeNextMap();
        this.convergePendingCount = new ConcurrentHashMap<>(dependencyBuilder.getConvergePendingCount());
        this.workerRouter = globalWorkerRouter;
        this.debugPublisher = debugPublisher;
    }

    public static Props props(NodeDependencyBuilder dependencyBuilder, ActorRef globalWorkerRouter,
                              Consumer<FlowContext> debugPublisher) {
        return Props.create(WorkflowInstanceActor.class,
                () -> new WorkflowInstanceActor(dependencyBuilder, globalWorkerRouter, debugPublisher));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(WorkflowProtocol.StartProcess.class, msg -> {
                    try {
                        handleStart(msg);
                    } catch (Throwable e) {
                        handleFatalError(e, null);
                    }
                }).match(WorkflowProtocol.NodeCompleted.class, msg -> {
                    try {
                        handleNodeCompleted(msg);
                    } catch (Throwable e) {
                        handleFatalError(e, msg.getNodeId());
                    }
                }).match(WorkflowProtocol.NodeFailed.class, this::handleNodeFailed)
                .match(WorkflowProtocol.ScheduleNextLoopIteration.class, msg -> {
                    try {
                        handleScheduleNextLoopIteration(msg);
                    } catch (Throwable e) {
                        handleFatalError(e, msg.getLoopNodeId());
                    }
                }).build();
    }

    /**
     * 处理调度器自身的致命异常
     */
    private void handleFatalError(Throwable e, String nodeId) {
        log.error("WorkflowInstanceActor 内部调度发生异常: {}", e.getMessage(), e);
        if (flowContext != null) {
            flowContext.addTraceLog(nodeId, "调度引擎内部异常: {}", e.getMessage());
        }
        if (caller != null) {
            caller.tell(new Status.Failure(e), getSelf());
        }
        getContext().stop(getSelf());
    }

    private void handleStart(WorkflowProtocol.StartProcess msg) {
        this.flowContext = msg.getFlowContext();
        this.caller = getSender();
        this.skipNodeIds = new HashSet<>(msg.getSkipNodeIds());
        Node startNode = msg.getRuleFlowModel().getStartNode();
        this.flowContext.addTraceLog(startNode.getId(), "规则流启动");
        if (log.isInfoEnabled()) {
            log.info("[{}] 规则流启动", startNode.getId());
        }
        scheduleNodeExecution(startNode, null);
    }

    private void handleNodeCompleted(WorkflowProtocol.NodeCompleted msg) {
        Node currentNode = msg.getNode();
        // 无论是来自 Worker 还是来自自身的跳过消息，都视为任务完成，减少计数器
        runningNodesCount--;
        if (msg.getScope() != null) {
            // --- 子流程逻辑 ---
            handleChildNodeCompleted(msg);
        } else {
            if (currentNode instanceof EndNode) {
                this.flowContext.addTraceLog(currentNode.getId(), "结束节点,执行完成,规则流分支结束");
                if (log.isDebugEnabled()) {
                    log.debug("[{}] 结束节点,执行完成,规则流分支结束", currentNode.getId());
                }
            } else {
                scheduleNextNodes(currentNode);
            }
            checkGlobalTermination();
        }
    }

    private void handleChildNodeCompleted(WorkflowProtocol.NodeCompleted msg) {
        WorkflowProtocol.ExecutionScope scope = msg.getScope();
        String loopNodeId = scope.getLoopNodeId();

        Integer currentCount = loopActiveChildCount.get(loopNodeId);
        if (currentCount != null) {
            int newCount = currentCount - 1;
            loopActiveChildCount.put(loopNodeId, newCount);
            LoopNode loopNode = activeLoopNodes.get(loopNodeId);

            if (loopNode != null) {
                List<Node> nextNodes = loopNode.getChildGraph().getNodeNextMap().get(msg.getNodeId());
                if (nextNodes != null && !nextNodes.isEmpty()) {
                    for (Node next : nextNodes) {
                        scheduleNodeExecution(next, scope);
                    }
                }

                if (loopActiveChildCount.get(loopNodeId) <= 0) {
                    LoopIterationState state = loopIterationStates.get(loopNodeId);
                    if (state != null && state.getCurrentIndex() + 1 < state.getCollection().size()) {
                        int nextIndex = state.getCurrentIndex() + 1;
                        Object nextItem = state.getCollection().get(nextIndex);

                        loopIterationStates.put(loopNodeId, new LoopIterationState(
                                state.getCollection(), nextIndex, state.getStartTime()
                        ));

                        if (Objects.nonNull(loopNode.getDelayTime()) && loopNode.getDelayTime() > 0) {
                            log.info("循环节点 [{}] 迭代 {} 完成，延时 {}ms 后开始下一次迭代",
                                    loopNode.getId(), scope.getIndex() + 1, loopNode.getDelayTime());
                            flowContext.addTraceLog(loopNode.getId(),
                                    "迭代 {} 完成，延时 {}ms 后开始下一次迭代",
                                    scope.getIndex() + 1, loopNode.getDelayTime());

                            getContext().getSystem().scheduler().scheduleOnce(
                                    Duration.create(loopNode.getDelayTime(), TimeUnit.MILLISECONDS),
                                    getSelf(),
                                    new WorkflowProtocol.ScheduleNextLoopIteration(
                                            loopNodeId, nextIndex, nextItem, state.getStartTime()
                                    ),
                                    getContext().getDispatcher(),
                                    getSelf()
                            );
                        } else {
                            log.info("循环节点 [{}] 迭代 {} 完成，立即开始下一次迭代",
                                    loopNode.getId(), scope.getIndex());
                            scheduleLoopIteration(loopNode, nextItem, nextIndex, state.getStartTime());
                        }
                    } else {
                        finishLoopNode(loopNode, state != null ? state.getStartTime() : msg.getStartTime(), System.nanoTime());
                    }
                }
            }
        }
    }

    private void finishLoopNode(LoopNode loopNode, Long startTime, Long endTime) {
        log.info("循环节点 [{}] 所有迭代执行完毕", loopNode.getId());
        flowContext.addTraceLog(loopNode.getId(), "循环处理完成");
        flowContext.removeVariable(loopNode.getId());

        loopActiveChildCount.remove(loopNode.getId());
        activeLoopNodes.remove(loopNode.getId());
        loopIterationStates.remove(loopNode.getId());

        runningNodesCount--;
        recordVirtualExecution(loopNode, ExecutStatus.SUCCESS, startTime, endTime);

        scheduleNextNodes(loopNode);

        checkGlobalTermination();
    }

    private void handleNodeFailed(WorkflowProtocol.NodeFailed msg) {
        Node failedNode = msg.getNode();
        // 失败的节点必然是 Worker 正在处理的节点，因此需要减计数
        runningNodesCount--;
        if (log.isErrorEnabled()) {
            log.error("执行失败 节点: {} 正在执行节点数:{}", failedNode.getId(), runningNodesCount, msg.getReason());
        }
        this.flowContext.addTraceLog(failedNode.getId(), "规则流异常中断: {}", msg.getReason().getMessage());

        // 异常中断，直接通知调用者失败并停止规则流
        if (caller != null) {
            caller.tell(new Status.Failure(msg.getReason()), getSelf());
        }
        getContext().stop(getSelf());
    }

    /**
     * 检查当前是否有正在执行的节点，如果没有，则终止整个规则流。
     */
    private void checkGlobalTermination() {
        if (runningNodesCount == 0) {
            this.flowContext.addTraceLog(null, "所有节点执行完毕");
            if (log.isDebugEnabled()) {
                log.debug("所有节点执行完毕");
            }
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
            this.flowContext.addTraceLog(currentNode.getId(), "无后续节点 规则流分支结束");
            if (log.isDebugEnabled()) {
                log.debug("[{}] 无后续节点 规则流分支结束", currentNode.getId());
            }
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
            this.flowContext.addTraceLog(targetNode.getId(), "并行网关触发后续调度");
            if (log.isDebugEnabled()) {
                log.debug("[{}] 并行网关触发后续调度", targetNode.getId());
            }
            recordVirtualExecution(targetNode, ExecutStatus.SUCCESS);
            scheduleNextNodes(targetNode);
            return;
        }
        if (targetNode instanceof InclusiveGatewayNode) {
            // dispatchNode 代表正常执行流，标记为 activePath = true
            handleInclusiveGateway((InclusiveGatewayNode) targetNode, true);
            return;
        }
        if (targetNode instanceof LoopNode) {
            handleLoopNode((LoopNode) targetNode);
            return;
        }
        scheduleNodeExecution(targetNode, null);
    }

    private void handleLoopNode(LoopNode loopNode) {
        Object collectionObj = VariableUtils.getByPathVariable(loopNode.getCollectionVariableName(), flowContext.getVariable());
        List<?> collection = (collectionObj instanceof List) ? (List<?>) collectionObj : Collections.emptyList();
        flowContext.addTraceLog(loopNode.getId(), "集合大小:{}", collection.size());
        if (collection.isEmpty()) {
            log.info("循环节点 [{}] 集合为空，跳过执行", loopNode.getId());
            recordVirtualExecution(loopNode, ExecutStatus.SUCCESS);
            scheduleNextNodes(loopNode);
            return;
        }
        long startTime = System.nanoTime();
        recordVirtualExecution(loopNode, ExecutStatus.RUNNING);

        runningNodesCount++;
        activeLoopNodes.put(loopNode.getId(), loopNode);
        loopActiveChildCount.put(loopNode.getId(), 0);
        loopIterationStates.put(loopNode.getId(), new LoopIterationState(collection, 0, startTime));

        log.info("循环节点 [{}] 开始调度，集合大小: {}", loopNode.getId(), collection.size());

        scheduleLoopIteration(loopNode, collection.get(0), 0, startTime);
    }

    private void scheduleLoopIteration(LoopNode loopNode, Object item, int index, long startTime) {
        Map<String, Object> vars = new HashMap<>();
        if (StringUtils.isNotBlank(loopNode.getLoopRowVariableName())) {
            vars.put(loopNode.getLoopRowVariableName(), item);
        }
        if (StringUtils.isNotBlank(loopNode.getLoopRowIndex())) {
            vars.put(loopNode.getLoopRowIndex(), index);
        }

        flowContext.setNodeOutput(loopNode.getId(), vars);

        WorkflowProtocol.ExecutionScope scope = WorkflowProtocol.ExecutionScope.builder()
                .startTime(startTime)
                .loopNodeId(loopNode.getId())
                .iterationId(loopNode.getId() + "_" + index)
                .index(index)
                .localVariables(vars)
                .build();

        if (loopNode.getStartNodes() != null) {
            for (Node startNode : loopNode.getStartNodes()) {
                scheduleNodeExecution(startNode, scope);
            }
        }
    }

    private void handleScheduleNextLoopIteration(WorkflowProtocol.ScheduleNextLoopIteration msg) {
        LoopNode loopNode = activeLoopNodes.get(msg.getLoopNodeId());
        if (loopNode == null) {
            log.warn("收到延时迭代消息，但循环节点 [{}] 已不存在", msg.getLoopNodeId());
            return;
        }

        log.info("循环节点 [{}] 延时结束，开始第 {} 次迭代", loopNode.getId(), msg.getNextIndex());
        flowContext.addTraceLog(loopNode.getId(), "延时结束，开始第 {} 次迭代", msg.getNextIndex());

        scheduleLoopIteration(loopNode, msg.getNextItem(), msg.getNextIndex(), msg.getStartTime());
    }

    private void handleExclusiveGateway(ExclusiveGatewayNode gateway) {
        List<SequenceConnNode> nextNodes = nodeNextMap.getOrDefault(gateway.getId(), new ArrayList<>()).stream().filter(node -> node instanceof SequenceConnNode).map(node -> (SequenceConnNode) node).sorted(Comparator.comparingInt(SequenceConnNode::getSortNum)).collect(Collectors.toList());
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
                    this.flowContext.addTraceLog(connector.getId(), "排他网关条件计算异常: {}", e.getMessage());
                    if (log.isDebugEnabled()) {
                        log.debug("[{}] 排他网关条件计算异常: {}", connector.getId(), e.getMessage());
                    }
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
                this.flowContext.addTraceLog(gateway.getId(), "排他网关命中节点: {}", connector.getId());
                if (log.isDebugEnabled()) {
                    log.debug("[{}] 排他网关命中节点: {}", gateway.getId(), connector.getId());
                }
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
            this.flowContext.addTraceLog(gateway.getId(), "排他网关无符合条件路径");

            if (log.isDebugEnabled()) {
                log.debug("[{}] 排他网关无符合条件路径", gateway.getId());
            }
        }
    }

    private void handleSequenceConnector(SequenceConnNode connector) {
        boolean shouldExecute = true;
        String exprLog = "";
        if (connector.getRule() != null) {
            String expr = connector.getRule().getExpressionCacheString();
            exprLog = expr;
            try {
                shouldExecute = ConditionUtil.resolve(expr, flowContext.getVariable());
            } catch (Exception e) {
                this.flowContext.addTraceLog(connector.getId(), "条件计算异常: {}", e.getMessage());
                if (log.isDebugEnabled()) {
                    log.debug("[{}] 条件计算异常: {}", connector.getId(), e.getMessage(), e);
                }
                shouldExecute = false;
            }
        }
        recordVirtualExecution(connector, ExecutStatus.SUCCESS, shouldExecute);
        if (StringUtils.isNotBlank(exprLog)) {
            this.flowContext.addTraceLog(connector.getId(), "条件表达式:\"{}\" 计算结果:{}", exprLog, shouldExecute);
            if (log.isDebugEnabled()) {
                log.debug("[{}] 条件表达式:\"{}\" 计算结果:{}", connector.getId(), exprLog, shouldExecute);
            }
        }
        if (shouldExecute) {
            scheduleNextNodes(connector);
        } else {
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
                if (log.isDebugEnabled()) {
                    log.debug("[{}] 聚合节点,剩余待完成分支:{}", gateway.getId(), pending);
                }
                this.flowContext.addTraceLog(gateway.getId(), "聚合节点,剩余待完成分支:{}", pending);

            }
            // 只有当所有分支（无论是执行还是跳过）都到达时，才进行下一步判断
            if (pending <= 0) {
                // 只有当网关被“激活”（至少有一个 Active 分支）时，才真正执行
                if (activatedGateways.contains(key)) {
                    if (log.isDebugEnabled()) {
                        log.debug("[{}] 聚合节点,并行分支完成,继续执行", gateway.getId());
                    }
                    this.flowContext.addTraceLog(gateway.getId(), "聚合节点,并行分支完成,继续执行");

                    recordVirtualExecution(gateway, ExecutStatus.SUCCESS);
                    scheduleNodeExecution(gateway, null);
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("[{}] 聚合节点所有前置分支均被跳过,该节点自动跳过", gateway.getId());
                    }
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
        long now = System.nanoTime();
        recordVirtualExecution(node, status, null, now, now);
    }

    private void recordVirtualExecution(Node node, ExecutStatus status, Boolean condition) {
        long now = System.nanoTime();
        recordVirtualExecution(node, status, condition, now, now);
    }

    private void recordVirtualExecution(Node node, ExecutStatus status, Long startTime, Long endTime) {
        recordVirtualExecution(node, status, null, startTime, endTime);
    }


    private void recordVirtualExecution(Node node, ExecutStatus status, Boolean condition, Long startTime, Long endTime) {
        NodeExcution.NodeExcutionBuilder builder = NodeExcution.builder().id(node.getId()).name(node.getName()).startTime(startTime).endTime(endTime).nodeType(node.getType()).status(status);
        if (condition != null) {
            builder.conditionsMeet(condition);
        }
        flowContext.putNodeExcution(node.getId(), builder.build());
        debugPublisher.accept(flowContext);
    }

    private void scheduleNodeExecution(Node node, WorkflowProtocol.ExecutionScope scope) {
        // 统一处理计数器：无论是跳过还是提交给 Worker，都视为一个需要等待完成的任务
        runningNodesCount++;
        long startTime = System.nanoTime();
        // 如果在循环内，更新循环的子任务计数
        if (scope != null) {
            loopActiveChildCount.merge(scope.getLoopNodeId(), 1, Integer::sum);
            startTime = scope.getStartTime();
        }
        // 检查是否需要跳过
        if (this.skipNodeIds.contains(node.getId())) {
            if (log.isDebugEnabled()) {
                log.debug("[{}] 指定跳过节点,跳过执行", node.getId());
            }
            this.flowContext.addTraceLog(node.getId(), "指定跳过节点,跳过执行");
            // 记录一条虚拟的执行记录，标记为 SUCCESS
            recordVirtualExecution(node, ExecutStatus.SUCCESS);
            // 触发完成从而驱动规则流继续向下 (Sender is self)
            getSelf().tell(new WorkflowProtocol.NodeCompleted(node.getId(), node, true, scope, startTime), getSelf());
            return;
        }
        // 路由池会自动选择一个空闲的 Worker (或者轮询) 来处理
        workerRouter.tell(new WorkflowProtocol.ExecuteNode(node, 0, startTime, flowContext, scope), getSelf());
    }

    /**
     * 循环迭代状态
     */
    @lombok.Value
    private static class LoopIterationState {
        List<?> collection;
        int currentIndex;
        long startTime;
    }
}