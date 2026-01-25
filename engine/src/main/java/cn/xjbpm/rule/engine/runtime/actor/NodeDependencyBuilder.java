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

import cn.xjbpm.rule.engine.definition.model.nodes.LoopNode;
import cn.xjbpm.rule.engine.definition.model.nodes.Node;
import cn.xjbpm.rule.engine.definition.model.nodes.SequenceConnNode;
import cn.xjbpm.rule.engine.definition.model.nodes.gateway.InclusiveGatewayNode;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 节点依赖关系构建器：负责解析节点集合，构建节点间的依赖关系映射
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Slf4j
public class NodeDependencyBuilder {
    // 当前层级的节点依赖关系
    private final Map<String, List<Node>> nodeNextMap;
    // 当前层级的聚合节点计数
    private final Map<String, Integer> convergePendingCount;
    // 当前层级的所有节点
    private final List<? extends Node> currentLevelNodes;

    public NodeDependencyBuilder(List<? extends Node> allNodes) {
        this.nodeNextMap = new ConcurrentHashMap<>();
        this.convergePendingCount = new ConcurrentHashMap<>();

        // 1. 根据 parentId 对节点进行分组
        // Key: parentId (null 为根层级), Value: List<Node>
        Map<String, List<Node>> groupedNodes = allNodes.stream()
                .collect(Collectors.groupingBy(node -> node.getParentId() == null ? "ROOT" : node.getParentId()));

        // 只有当传入的节点列表中包含 parentId 为 null 的节点时，我们才视为这是“顶级构建”，需要执行拆分逻辑。
        boolean isRootBuild = allNodes.stream().anyMatch(n -> n.getParentId() == null);

        if (isRootBuild) {
            this.currentLevelNodes = groupedNodes.getOrDefault("ROOT", new ArrayList<>());
            // 处理根层级依赖
            buildDependency(this.currentLevelNodes);

            // 3. 递归处理子流程 (LoopNode)
            Map<String, Node> nodeMap = allNodes.stream()
                    .collect(Collectors.toMap(Node::getId, Function.identity(), (a, b) -> a));

            groupedNodes.forEach((parentId, children) -> {
                if ("ROOT".equals(parentId)) return;

                Node parentNode = nodeMap.get(parentId);
                if (parentNode instanceof LoopNode) {
                    LoopNode loopNode = (LoopNode) parentNode;
                    NodeDependencyBuilder childBuilder = new NodeDependencyBuilder(children);
                    loopNode.setChildGraph(childBuilder);
                    // 计算子流程的起始节点（入度为0的节点）
                    loopNode.setStartNodes(findStartNodes(children));
                }
            });
        } else {
            // 如果传入的都是有 parentId 的（即都在同一层级），直接构建
            this.currentLevelNodes = allNodes;
            buildDependency(allNodes);
        }
    }

    /**
     * 构建节点依赖关系：解析所有节点的连接器，建立from -> to映射
     */
    private void buildDependency(List<? extends Node> nodes) {
        long l = System.currentTimeMillis();
        for (Node node : nodes) {
            List<SequenceConnNode> outgoingNodes = node.getOutgoingNodes();
            if (outgoingNodes == null) {
                continue;
            }
            for (Node connector : outgoingNodes) {
                // 仅当目标节点也在当前层级时，才建立连接关系
                // 防止主流程节点错误连接到子流程节点，反之亦然
                nodeNextMap.computeIfAbsent(node.getId(), k -> new ArrayList<>()).add(connector);

                if (connector instanceof SequenceConnNode) {
                    Node nodeTarget = ((SequenceConnNode) connector).getTargetNode();
                    if (nodeTarget != null) {
                        // 确保目标节点属于当前层级（通过判断集合包含性）或者简单放行（依赖设计器保证）
                        // 这里直接建立连接
                        nodeNextMap.computeIfAbsent(connector.getId(), k -> new ArrayList<>()).add(nodeTarget);

                        if (nodeTarget instanceof InclusiveGatewayNode) {
                            convergePendingCount.compute(nodeTarget.getId(), (k, count) -> count == null ? 1 : count + 1);
                        }
                    }
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("层级节点依赖构建完成 节点数:{} 耗时：{}ms", nodes.size(), System.currentTimeMillis() - l);
        }
    }

    /**
     * 查找入度为0的节点作为子流程起始点
     */
    private List<Node> findStartNodes(List<? extends Node> nodes) {
        // 统计所有被指向的节点ID
        List<String> targetNodeIds = new ArrayList<>();
        for (Node node : nodes) {
            if (node.getOutgoingNodes() != null) {
                for (SequenceConnNode conn : node.getOutgoingNodes()) {
                    if (conn.getTargetNode() != null) {
                        targetNodeIds.add(conn.getTargetNode().getId());
                    }
                }
            }
        }
        // 排除掉连接线(SequenceConnNode)，只返回真正的功能节点
        return nodes.stream()
                .filter(n -> !(n instanceof SequenceConnNode))
                .filter(n -> !targetNodeIds.contains(n.getId()))
                .collect(Collectors.toList());
    }


    /**
     * 获取节点的后续节点映射
     */
    public Map<String, List<Node>> getNodeNextMap() {
        return nodeNextMap;
    }

    /**
     * 获取聚合节点的未完成分支数映射
     */
    public Map<String, Integer> getConvergePendingCount() {
        return convergePendingCount;
    }
}