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
package cn.xjbpm.rule.engine.definition.parse;

import cn.xjbpm.rule.common.utils.JsonUtils;
import cn.xjbpm.rule.engine.definition.model.*;
import cn.xjbpm.rule.engine.runtime.behavior.BehaviorFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/29
 */
@Slf4j
public class RuleFlowModelParse {

    /**
     * 转换成模型
     *
     * @param json 流程定义的 JSON 字符串
     * @return 转换后的 RuleFlowModel
     */
    @SuppressWarnings("unchecked")
    public RuleFlowModel convertToModel(String json) {
        try {
            // 1. 解析根 Map
            Map<String, Object> rootMap = JsonUtils.json2Obj(json, Map.class);
            if (CollectionUtils.isEmpty(rootMap)) {
                return new RuleFlowModel();
            }
            // 2. 转换 ProcessModel 属性 (使用高效的 convertValue)
            RuleFlowModel ruleFlowModel = JsonUtils.convertValue(rootMap, RuleFlowModel.class);
            if (ruleFlowModel == null) {
                ruleFlowModel = new RuleFlowModel();
            }
            ruleFlowModel.setKey(getString(rootMap.get("key")));
            ruleFlowModel.setName(getString(rootMap.get("name")));
            ruleFlowModel.setDescription(getString(rootMap.get("description")));
            ruleFlowModel.setOriginalJson(json);
            // 3. 转换 Business Object Models
            List<Map<String, Object>> businessObjectModelsMap = (List<Map<String, Object>>) rootMap.get("businessObjectModels");
            if (!CollectionUtils.isEmpty(businessObjectModelsMap)) {
                // 使用高效的 convertValueToList
                List<ObjectModel> objectModels = JsonUtils.convertValueToList(businessObjectModelsMap, ObjectModel.class);
                ruleFlowModel.setBusinessObjectModels(objectModels);
            }

            List<Node> nodes = new ArrayList<>();
            Map<String, Node> nodeMap = new HashMap<>();

            List<Map<String, Object>> nodesObjects = (List<Map<String, Object>>) rootMap.get("nodes");
            if (nodesObjects == null) {
                nodesObjects = Collections.emptyList();
            }

            // 4. 解析普通节点 (使用高效的 convertValue)
            for (Map<String, Object> map : nodesObjects) {
                // 必须先转换 NodeType，因为它是决定目标类 Class 的关键
                NodeType type = JsonUtils.convertValue(map.get("type"), NodeType.class);
                Map<String, Object> properties = (Map<String, Object>) map.get("properties");


                Node node = JsonUtils.convertValue(properties, (Class<Node>) type.getNodeClass());

                // 填充基础属性
                node.setOriginalJson(JsonUtils.obj2Json(map));
                node.setType(type);
                node.setId(getString(map.get("id")));

                Map<String, Object> text = (Map<String, Object>) map.get("text");
                node.setName(Objects.nonNull(text) && text.containsKey("value") ? getString(text.get("value")) : null);
                node.setDocumentation(getString(properties.get("name")));
                node.setTag(getString(properties.get("tag")));

                node.setMaxRetries(getIntValueFromMap(properties, "maxRetries", 0));
                node.setRetryDelay(getIntValueFromMap(properties, "retryDelay", 0));

                if (type == NodeType.StartNode) {
                    ruleFlowModel.setStartNode((StartNode) node);
                } else if (type == NodeType.EndNode) {
                    ruleFlowModel.setEndNode((EndNode) node);
                }
                nodes.add(node);
                nodeMap.put(node.getId(), node);
            }

            // 5. 解析连接线 (Edge)
            List<Map<String, Object>> edgeJsonArray = (List<Map<String, Object>>) rootMap.get("edges");
            if (edgeJsonArray == null) {
                edgeJsonArray = Collections.emptyList();
            }

            List<SequenceConnNode> sequenceConnNodes = new ArrayList<>();
            for (Map<String, Object> map : edgeJsonArray) {
                NodeType type = JsonUtils.convertValue(map.get("type"), NodeType.class);
                SequenceConnNode node = JsonUtils.convertValue(map, (Class<SequenceConnNode>) type.getNodeClass());

                node.setOriginalJson(JsonUtils.obj2Json(map));
                node.setType(type);

                Map<String, Object> properties = (Map<String, Object>) map.get("properties");
                Map<String, Object> text = (Map<String, Object>) map.get("text");

                node.setName(Objects.nonNull(text) ? getString(text.get("value")) : null);
                node.setDocumentation(getString(properties.get("name")));

                node.setSourceNodeKey(getString(map.get("sourceNodeId")));
                node.setTargetNodeKey(getString(map.get("targetNodeId")));
                node.setSourceNode(nodeMap.get(node.getSourceNodeKey()));
                node.setTargetNode(nodeMap.get(node.getTargetNodeKey()));

                nodes.add(node);
                nodeMap.put(node.getId(), node);

                sequenceConnNodes.add(node);
            }

            for (SequenceConnNode sequenceConnNode : sequenceConnNodes) {
                Node sourceNode = sequenceConnNode.getSourceNode();
                Node targetNode = sequenceConnNode.getTargetNode();

                if (sourceNode != null) {
                    List<SequenceConnNode> sourceNodeOutgoingNodes = !CollectionUtils.isEmpty(sourceNode.getOutgoingNodes()) ? sourceNode.getOutgoingNodes() : new ArrayList<>();
                    sourceNodeOutgoingNodes.add(sequenceConnNode);
                    sourceNode.setOutgoingNodes(sourceNodeOutgoingNodes);
                }

                if (targetNode != null) {
                    List<SequenceConnNode> targetNodeIncomingNodes = !CollectionUtils.isEmpty(targetNode.getIncomingNodes()) ? targetNode.getIncomingNodes() : new ArrayList<>();
                    targetNodeIncomingNodes.add(sequenceConnNode);
                    targetNode.setIncomingNodes(targetNodeIncomingNodes);
                }
            }

            ruleFlowModel.setChildNodes(nodes);
            setBehavior(ruleFlowModel);
            return ruleFlowModel;
        } catch (Exception e) {
            log.error("规则流图转换规则模型失败", e);
            throw new RuntimeException("规则流图转换规则模型失败", e);
        }
    }

    /**
     * 辅助方法：从 Map 中安全获取 Integer 值，兼容 Jackson 将数字解析为 Number 的问题
     */
    private int getIntValueFromMap(Map<String, Object> map, String key, int defaultValue) {
        if (map == null || !map.containsKey(key)) {
            return defaultValue;
        }
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }

    private String getString(Object object) {
        if (object == null) {
            return null;
        }
        return object.toString();
    }

    /**
     * 设置行为处理
     */
    private void setBehavior(RuleFlowModel ruleFlowModel) {
        List<? extends Node> childNodes = ruleFlowModel.getChildNodes();
        if (!CollectionUtils.isEmpty(childNodes)) {
            for (Node childNode : childNodes) {
                NodeType type = childNode.getType();
                childNode.setBehavior(BehaviorFactory.createBehavior(type, childNode));
                // 排序
                if (childNode.getOutgoingNodes() != null) {
                    childNode.setOutgoingNodes(
                            childNode.getOutgoingNodes().stream()
                                    .sorted(Comparator.comparing(Node::getOrder))
                                    .collect(Collectors.toList())
                    );
                }
            }
        }
    }


    /**
     * 转换成json
     *
     * @param ruleFlowModel
     * @return
     */
    @SuppressWarnings("unchecked")
    public String convertToJson(RuleFlowModel ruleFlowModel) {
        Map<String, Object> stringObjectMap = JsonUtils.convertValue(ruleFlowModel, Map.class);

        stringObjectMap.remove("childNodes");
        stringObjectMap.remove("startNode");
        stringObjectMap.remove("endNode");

        List<Map> nodes = new ArrayList<>();
        List<Map> edges = new ArrayList<>();

        List<? extends Node> childNodes = ruleFlowModel.getChildNodes();
        if (!CollectionUtils.isEmpty(childNodes)) {
            for (Node node : childNodes) {
                // 读取原始 JSON 字符串并解析为 Map
                Map<String, Object> originalJsonMap = JsonUtils.json2Obj(node.getOriginalJson(), Map.class);
                if (node instanceof SequenceConnNode) {
                    edges.add(originalJsonMap);
                } else {
                    nodes.add(originalJsonMap);
                }
            }
        }
        stringObjectMap.put("nodes", nodes);
        stringObjectMap.put("edges", edges);
        return JsonUtils.obj2Json(stringObjectMap);
    }
}