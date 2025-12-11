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
package cn.xjbpm.rule.common.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.xjbpm.rule.engine.definition.model.ObjectModel;
import cn.xjbpm.rule.engine.rule.enums.VariableType;

import java.util.*;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/6/30
 */
@SuppressWarnings("all")
public class VariableTranslateUtils {
    public static Map<String, Object> translate(List<ObjectModel> businessObjectModels, boolean response, Map<String, Object> variable) {
        if (variable == null) {
            return new HashMap<>();
        }
        return translateInternal(businessObjectModels, response, variable);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> translateInternal(List<ObjectModel> models, boolean response, Map<String, Object> sourceMap) {
        Map<String, Object> targetMap = new HashMap<>(models.size());

        for (ObjectModel model : models) {
            // 1. 过滤不需要返回的字段
            if (response && !model.isResponse()) {
                continue;
            }

            // 2. 确定取值的 Key
            // 如果是 response 模式，通常意味着从内部 key 映射出去；如果是 request 模式，是从外部 key 映射进来。
            String sourceKey = response ? model.getLabel() : model.getValue();
            Object sourceValue = sourceMap.get(sourceKey);

            // 3. 非必须且为 null，直接跳过
            if (!model.isRequired() && sourceValue == null) {
                continue;
            }

            // 4. 校验 (优化：避免 String.format 的预计算)
            checkNotNull(model, sourceValue);

            // 5. 确定目标 Key
            String targetKey = response ? model.getValue() : model.getLabel();

            // 6. 递归处理或直接赋值
            if (model.getType() == VariableType.OBJECT) {
                if (sourceValue instanceof Map) {
                    Map<String, Object> childResult = translateInternal(model.getChildren(), response, (Map<String, Object>) sourceValue);
                    targetMap.put(targetKey, childResult);
                }
            } else if (model.getType() == VariableType.LIST) {
                if (sourceValue instanceof Collection) {
                    List<Object> listResult = translateList(model, response, (Collection<?>) sourceValue);
                    targetMap.put(targetKey, listResult);
                }
            } else {
                // 基础类型直接赋值 (如果需要类型强转，可以在这里结合 VariableType 的策略)
                targetMap.put(targetKey, sourceValue);
            }
        }
        return targetMap;
    }

    @SuppressWarnings("unchecked")
    private static List<Object> translateList(ObjectModel model, boolean response, Collection<?> sourceList) {
        List<Object> resultList = new ArrayList<>(sourceList.size());
        List<ObjectModel> childrenModels = model.getChildren();

        if (CollUtil.isEmpty(childrenModels)) {
            // 如果没有定义子结构，直接拷贝值
            resultList.addAll(sourceList);
            return resultList;
        }

        for (Object item : sourceList) {
            if (item instanceof Map) {
                // 列表中的每个对象都需要递归转换
                Map<String, Object> convertedItem = translateInternal(childrenModels, response, (Map<String, Object>) item);
                resultList.add(convertedItem);
            }
        }
        return resultList;
    }

    private static void checkNotNull(ObjectModel model, Object value) {
        if (!model.isRequired()) {
            return;
        }

        // 仅在 value 为 null 时才进入异常抛出逻辑，避免 String.format 带来的开销
        if (value == null) {
            throw new IllegalArgumentException(String.format("[%s]字段不能为空", model.getLabel()));
        }

        VariableType type = model.getType();

        // 简单的类型检查
        if (type == VariableType.LIST) {
            if (!(value instanceof Collection)) {
                throw new IllegalArgumentException(String.format("[%s]字段必须为集合", model.getLabel()));
            }
            if (CollUtil.isEmpty((Collection<?>) value)) {
                throw new IllegalArgumentException(String.format("[%s]集合不能为空", model.getLabel()));
            }
        } else if (type == VariableType.OBJECT) {
            if (!(value instanceof Map)) {
                throw new IllegalArgumentException(String.format("[%s]字段必须为对象", model.getLabel()));
            }
            if (CollUtil.isEmpty((Map<?, ?>) value)) {
                throw new IllegalArgumentException(String.format("[%s]对象下级属性不能为空", model.getLabel()));
            }
        } else if (type == VariableType.STRING) {
            if (value instanceof String && StrUtil.isBlank((String) value)) {
                throw new IllegalArgumentException(String.format("[%s]字段不能为空", model.getLabel()));
            }
        }
    }


}