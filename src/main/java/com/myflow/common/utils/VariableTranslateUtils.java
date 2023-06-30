package com.myflow.common.utils;

import cn.hutool.core.collection.CollUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.myflow.definition.model.ObjectModel;
import com.myflow.rule.enums.VariableType;
import lombok.SneakyThrows;

import java.util.List;
import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/6/30
 */
public class VariableTranslateUtils {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new Jdk8Module())
            .registerModule(new JavaTimeModule()).setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

    @SneakyThrows
    public static Map<String, Object> translate(List<ObjectModel> businessObjectModels, String jsonData) {
        // 创建 ObjectMapper 实例
        // 解析 JSON 数据
        JsonNode jsonNode = OBJECT_MAPPER.readTree(jsonData);
        // 创建目标 JSON 数据对象
        ObjectNode targetNode = OBJECT_MAPPER.createObjectNode();
        for (ObjectModel businessObjectModel : businessObjectModels) {
            JsonNode currentNode = jsonNode.get(businessObjectModel.getValue());
            translate(businessObjectModel, currentNode, targetNode);
        }
        return OBJECT_MAPPER.convertValue(targetNode, Map.class);
    }


    private static void translate(ObjectModel businessObjectModel, JsonNode currentNode, ObjectNode targetNode) {
        if (businessObjectModel.getType() == VariableType.OBJECT) {
            translateObject(businessObjectModel, currentNode, targetNode);
        } else if (businessObjectModel.getType() == VariableType.LIST) {
            translateArray(businessObjectModel, currentNode, targetNode);
        } else {
            translateBaseNode(businessObjectModel, currentNode, targetNode);
        }
    }

    private static void translateBaseNode(ObjectModel businessObjectModel, JsonNode currentNode, ObjectNode targetNode) {
        targetNode.set(businessObjectModel.getLabel(), currentNode);
    }


    private static void translateObject(ObjectModel businessObjectModel, JsonNode currentNode, ObjectNode targetNode) {
        ObjectNode tempNode = OBJECT_MAPPER.createObjectNode();
        List<ObjectModel> children = businessObjectModel.getChildren();
        if (CollUtil.isNotEmpty(children)) {
            for (ObjectModel child : children) {
                JsonNode childNode = currentNode.get(child.getValue());
                translate(child, childNode, tempNode);

            }
        }
        targetNode.set(businessObjectModel.getLabel(), tempNode);
    }

    private static void translateArray(ObjectModel businessObjectModel, JsonNode currentNode, ObjectNode targetNode) {
        ArrayNode arrayNode = (ArrayNode) currentNode;
        ArrayNode tempArrayNode = OBJECT_MAPPER.createArrayNode();
        for (JsonNode arrChildNode : arrayNode) {
            List<ObjectModel> children = businessObjectModel.getChildren();
            if (CollUtil.isNotEmpty(children)) {
                ObjectNode tempNode = OBJECT_MAPPER.createObjectNode();
                for (ObjectModel child : children) {
                    JsonNode childNode = arrChildNode.get(child.getValue());
//                    tempNode.set(child.getLabel(), childNode);

                    translate(child, childNode, tempNode);
                }
                tempArrayNode.add(tempNode);
            }
        }
        targetNode.set(businessObjectModel.getLabel(), tempArrayNode);
    }

}
