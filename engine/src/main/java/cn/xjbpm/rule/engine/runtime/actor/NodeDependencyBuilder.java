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

import cn.xjbpm.rule.engine.definition.model.Node;
import cn.xjbpm.rule.engine.definition.model.SequenceConnNode;
import cn.xjbpm.rule.engine.definition.model.gateway.InclusiveGatewayNode;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 节点依赖关系构建器：负责解析节点集合，构建节点间的依赖关系映射
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Slf4j
public class NodeDependencyBuilder {
    private final Map<String, List<Node>> nodeNextMap;
    private final Map<String, Integer> convergePendingCount;

    public NodeDependencyBuilder() {
        this.nodeNextMap = new ConcurrentHashMap<>();
        this.convergePendingCount = new ConcurrentHashMap<>();
    }

    /**
     * 构建节点依赖关系：解析所有节点的连接器，建立from -> to映射
     */
    public void buildNodeDependency(List<? extends Node> allNodes) {
        for (Node node : allNodes) {
            // 遍历节点的所有输出连接器
            List<SequenceConnNode> outgoingNodes = node.getOutgoingNodes();
            if (outgoingNodes == null) {
                continue;
            }
            for (Node connector : outgoingNodes) {
                // 将节点与连接器关联
                nodeNextMap.computeIfAbsent(node.getId(), k -> new ArrayList<>()).add(connector);
                if (connector instanceof SequenceConnNode) {
                    // 双重校验：连接器的 target 也可能为 null
                    Node nodeTarget = ((SequenceConnNode) connector).getTargetNode();
                    if (nodeTarget != null) {
                        // 将连接器与目标节点关联
                        nodeNextMap.computeIfAbsent(connector.getId(), k -> new ArrayList<>()).add(nodeTarget);
                        // 特殊处理：聚合节点（InclusiveGatewayNode）需要知道并行分支数
                        if (nodeTarget instanceof InclusiveGatewayNode) {
                            convergePendingCount.compute(nodeTarget.getId(), (k, count) -> count == null ? 1 : count + 1);
                        }
                    }
                }
            }
        }
        log.info("节点依赖关系构建完成");
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