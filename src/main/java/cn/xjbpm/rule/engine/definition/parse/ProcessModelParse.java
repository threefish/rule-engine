package cn.xjbpm.rule.engine.definition.parse;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.xjbpm.rule.engine.definition.model.*;
import cn.xjbpm.rule.engine.runtime.BehaviorFactory;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/29
 */
public class ProcessModelParse {


    /**
     * 转换成模型
     *
     * @param json
     * @return
     */
    public ProcessModel convertToModel(String json) {
        List<Node> nodes = new ArrayList<>();
        JSONObject parse = JSONUtil.parseObj(json);
        ProcessModel processModel = parse.toBean(ProcessModel.class);
        JSONArray nodeJsonArray = parse.getJSONArray("nodes");
        JSONArray edgeJsonArray = parse.getJSONArray("edges");
        JSONArray businessObjectModels = parse.getJSONArray("businessObjectModels");
        if (businessObjectModels != null) {
            processModel.setBusinessObjectModels(businessObjectModels.toList(ObjectModel.class));
        }
        Map<String, Node> nodeMap = new HashMap<>(nodeJsonArray.size());

        for (Object object : nodeJsonArray) {
            JSONObject jsonObject = (JSONObject) object;
            NodeType type = jsonObject.get("type", NodeType.class);
            JSONObject properties = jsonObject.getJSONObject("properties");
            JSONObject text = jsonObject.getJSONObject("text");
            Node node = properties.toBean((Type) type.getNodeClass());
            node.setOriginalJson(jsonObject.toString());
            node.setType(type);
            node.setKey(jsonObject.getStr("id"));
            node.setName(text.getStr("value"));
            node.setName(text.getStr("value"));
            node.setDocumentation(properties.getStr("name"));
            node.setTag(properties.getStr("tag"));

            if (type == NodeType.StartNode) {
                processModel.setStartNode((StartNode) node);
            } else if (type == NodeType.EndNode) {
                processModel.setEndNode((EndNode) node);
            }

            nodes.add(node);
            nodeMap.put(node.getKey(), node);
        }

        List<SequenceConnNode> sequenceConnNodes = new ArrayList<>();
        for (Object object : edgeJsonArray) {
            JSONObject jsonObject = (JSONObject) object;
            NodeType type = jsonObject.get("type", NodeType.class);
            JSONObject properties = jsonObject.getJSONObject("properties");
            JSONObject text = jsonObject.getJSONObject("text");
            SequenceConnNode node = properties.toBean((Type) type.getNodeClass());
            node.setOriginalJson(jsonObject.toString());
            node.setType(type);
            node.setKey(jsonObject.getStr("id"));
            node.setName(Objects.nonNull(text) ? text.getStr("value") : null);
            node.setDocumentation(properties.getStr("name"));

            node.setSourceNodeKey(jsonObject.getStr("sourceNodeId"));
            node.setTargetNodeKey(jsonObject.getStr("targetNodeId"));
            node.setSourceNode(nodeMap.get(node.getSourceNodeKey()));
            node.setTargetNode(nodeMap.get(node.getTargetNodeKey()));

            nodes.add(node);
            nodeMap.put(node.getKey(), node);

            sequenceConnNodes.add(node);
        }

        for (SequenceConnNode sequenceConnNode : sequenceConnNodes) {
            String sourceNodeKey = sequenceConnNode.getSourceNodeKey();
            String targetNodeKey = sequenceConnNode.getTargetNodeKey();

            Node sourceNode = nodeMap.get(sourceNodeKey);
            Node targetNode = nodeMap.get(targetNodeKey);


            List sourceNodeOutgoingNodes = CollUtil.isNotEmpty(sourceNode.getOutgoingNodes()) ? sourceNode.getOutgoingNodes() : new ArrayList();
            sourceNodeOutgoingNodes.add(sequenceConnNode);
            sourceNode.setOutgoingNodes(sourceNodeOutgoingNodes);

            List targetNodeIncomingNodes = CollUtil.isNotEmpty(targetNode.getIncomingNodes()) ? targetNode.getIncomingNodes() : new ArrayList();
            targetNodeIncomingNodes.add(sequenceConnNode);
            targetNode.setIncomingNodes(targetNodeIncomingNodes);
        }

        processModel.setChildNodes(nodes);
        setBehavior(processModel);
        return processModel;
    }


    /**
     * 设置行为处理
     */
    private void setBehavior(ProcessModel processModel) {
        List<? extends Node> childNodes = processModel.getChildNodes();
        for (Node childNode : childNodes) {
            NodeType type = childNode.getType();
            childNode.setBehavior(BehaviorFactory.createBehavior(type, childNode));
            // 排序
            childNode.getOutgoingNodes().stream().sorted(Comparator.comparing(Node::getOrder)).collect(Collectors.toList());
        }
    }

    /**
     * 转换成json
     *
     * @param processModel
     * @return
     */
    public String convertToJson(ProcessModel processModel) {
        Map<String, Object> stringObjectMap = BeanUtil.beanToMap(processModel);
        stringObjectMap.remove("childNodes");
        stringObjectMap.remove("startNode");
        stringObjectMap.remove("endNode");

        List<Map> nodes = new ArrayList<>();
        List<Map> edges = new ArrayList<>();

        List<? extends Node> childNodes = processModel.getChildNodes();
        if (!CollectionUtils.isEmpty(childNodes)) {
            for (Node childNode : childNodes) {
                if (childNode instanceof SequenceConnNode) {
                    edges.add(JSONUtil.toBean(childNode.getOriginalJson(), Map.class));
                } else {
                    nodes.add(JSONUtil.toBean(childNode.getOriginalJson(), Map.class));
                }
            }
        }
        stringObjectMap.put("nodes", nodes);
        stringObjectMap.put("edges", edges);
        return JSONUtil.toJsonStr(stringObjectMap);
    }
}
