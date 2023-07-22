package cn.xjbpm.rule.engine.common.utils;

import cn.hutool.core.collection.CollUtil;
import cn.xjbpm.rule.engine.definition.model.ObjectModel;
import cn.xjbpm.rule.engine.rule.enums.VariableType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/6/30
 */
@SuppressWarnings("all")
public class VariableTranslateUtils {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new Jdk8Module())
            .registerModule(new JavaTimeModule()).setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

    @SneakyThrows
    public static Map<String, Object> translate(List<ObjectModel> businessObjectModels, boolean response, String jsonData) {
        JsonNode jsonNode = OBJECT_MAPPER.readTree(jsonData);
        ObjectNode targetNode = OBJECT_MAPPER.createObjectNode();
        for (ObjectModel businessObjectModel : businessObjectModels) {
            if (response && businessObjectModel.isResponse() == false) {
                continue;
            }
            JsonNode currentNode = jsonNode.get(businessObjectModel.getValue());
            if (response) {
                currentNode = jsonNode.get(businessObjectModel.getLabel());
            }
            checkNotNull(businessObjectModel, currentNode);
            translate(businessObjectModel, response, currentNode, targetNode);
        }
        return OBJECT_MAPPER.convertValue(targetNode, Map.class);
    }

    private static void checkNotNull(ObjectModel businessObjectModel, JsonNode jsonNode) {
        System.out.println(jsonNode == null);
        VariableType type = businessObjectModel.getType();
        Assert.notNull(jsonNode, String.format("[%s]字段不能为空", businessObjectModel.getLabel()));
        if (type == VariableType.LIST) {
            Assert.isTrue(jsonNode instanceof ArrayNode, String.format("[%s]字段必须为集合", businessObjectModel.getLabel()));
            ArrayNode arrayNode = ((ArrayNode) jsonNode);
            Assert.isTrue(!arrayNode.isEmpty(), String.format("[%s]集合不能为空", businessObjectModel.getLabel()));
        }
        if (type == VariableType.OBJECT) {
            Assert.isTrue(jsonNode instanceof ObjectNode, String.format("[%s]字段必须为对象", businessObjectModel.getLabel()));
            ObjectNode objectNode = ((ObjectNode) jsonNode);
            Assert.isTrue(!objectNode.isEmpty(), String.format("[%s]对象下级属性不能为空", businessObjectModel.getLabel()));
        }
    }

    private static void translate(ObjectModel businessObjectModel, boolean response, JsonNode currentNode, ObjectNode targetNode) {
        if (businessObjectModel.getType() == VariableType.OBJECT) {
            translateObject(businessObjectModel, response, currentNode, targetNode);
        } else if (businessObjectModel.getType() == VariableType.LIST) {
            translateArray(businessObjectModel, response, currentNode, targetNode);
        } else {
            translateBaseNode(businessObjectModel, response, currentNode, targetNode);
        }
    }

    private static void translateBaseNode(ObjectModel businessObjectModel, boolean response, JsonNode currentNode, ObjectNode targetNode) {
        if (!response) {
            targetNode.set(businessObjectModel.getLabel(), currentNode);
        } else {
            targetNode.set(businessObjectModel.getValue(), currentNode);
        }
    }


    private static void translateObject(ObjectModel businessObjectModel, boolean response, JsonNode currentNode, ObjectNode targetNode) {
        ObjectNode tempNode = OBJECT_MAPPER.createObjectNode();
        List<ObjectModel> children = businessObjectModel.getChildren();
        if (CollUtil.isNotEmpty(children)) {
            for (ObjectModel child : children) {
                if (response && child.isResponse() == false) {
                    continue;
                }
                JsonNode childNode = currentNode.get(child.getValue());
                if (response) {
                    childNode = currentNode.get(child.getLabel());
                }
                checkNotNull(child, childNode);
                translate(child, response, childNode, tempNode);
            }
        }
        if (!response) {
            targetNode.set(businessObjectModel.getLabel(), tempNode);
        } else {
            targetNode.set(businessObjectModel.getValue(), tempNode);
        }
    }

    private static void translateArray(ObjectModel businessObjectModel, boolean response, JsonNode currentNode, ObjectNode targetNode) {
        if (currentNode == null) {
            return;
        }
        Assert.isTrue(currentNode instanceof ArrayNode, String.format("[%s]字段实际内容不是list格式！", businessObjectModel.getLabel(), businessObjectModel.getType()));
        if (currentNode instanceof ArrayNode) {
            ArrayNode arrayNode = (ArrayNode) currentNode;
            ArrayNode tempArrayNode = OBJECT_MAPPER.createArrayNode();
            for (JsonNode arrChildNode : arrayNode) {
                List<ObjectModel> children = businessObjectModel.getChildren();
                if (CollUtil.isNotEmpty(children)) {
                    ObjectNode tempNode = OBJECT_MAPPER.createObjectNode();
                    for (ObjectModel child : children) {
                        if (response && child.isResponse() == false) {
                            continue;
                        }
                        JsonNode childNode = arrChildNode.get(child.getValue());
                        if (response) {
                            childNode = arrChildNode.get(child.getLabel());
                        }
                        checkNotNull(child, childNode);
                        translate(child, response, childNode, tempNode);
                    }
                    tempArrayNode.add(tempNode);
                }
            }
            if (!response) {
                targetNode.set(businessObjectModel.getLabel(), tempArrayNode);
            } else {
                targetNode.set(businessObjectModel.getValue(), tempArrayNode);
            }
        }
    }


}
