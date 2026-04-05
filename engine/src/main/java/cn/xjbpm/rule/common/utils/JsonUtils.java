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

package cn.xjbpm.rule.common.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.experimental.UtilityClass;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * @author 黄川 huchuc@vip.qq.com
 */
public class JsonUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new Jdk8Module())
            .registerModule(new JavaTimeModule()).setSerializationInclusion(JsonInclude.Include.ALWAYS)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

    /**
     * json到对象
     *
     * @param content
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T json2Obj(String content, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(content, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Json反序列化出错", e);
        }
    }

    /**
     * json到对象
     *
     * @param content
     * @param javaType
     * @param <T>
     * @return
     */
    public static <T> T json2Obj(String content, JavaType javaType) {
        try {
            return OBJECT_MAPPER.readValue(content, javaType);
        } catch (JsonProcessingException var3) {
            throw new RuntimeException("Json反序列化出错", var3);
        }
    }

    /**
     * 对象到json
     *
     * @param obj
     * @return
     */
    public static String obj2Json(Object obj) {
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Json序列化出错", e);
        }
    }

    /**
     * json到对象
     *
     * @param content
     * @param clazzItem
     * @param <T>
     * @return
     */
    public static <T> List<T> json2List(String content, Class<T> clazzItem) {
        try {
            if (!StringUtils.hasText(content)) {
                return null;
            }
            JavaType javaType = OBJECT_MAPPER.getTypeFactory()
                    .constructParametricType(List.class, clazzItem);
            return OBJECT_MAPPER.readValue(content, javaType);
        } catch (IOException e) {
            throw new RuntimeException("Json反序列化出错", e);
        }
    }

    public static <T> T convertValue(Object fromValue, Class<T> toValueType) {
        return OBJECT_MAPPER.convertValue(fromValue, toValueType);
    }

    /**
     * List<Map> 到 List<T> 的高效转换
     *
     * @param fromValue
     * @param elementClass
     * @param <T>
     * @return
     */
    public static <T> List<T> convertValueToList(Object fromValue, Class<T> elementClass) {
        JavaType listType = OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, elementClass);
        return OBJECT_MAPPER.convertValue(fromValue, listType);
    }


    /**
     * 使用 Jackson 库执行专业、高性能的 JSON 字符串转义。
     *
     * @param originalString 原始字符串
     * @return 经过转义的字符串（不包含首尾双引号）
     */
    public static String escapeJsonString(String originalString) {
        if (originalString == null) {
            return null;
        }
        try {
            String escapedAndQuoted = OBJECT_MAPPER.writeValueAsString(originalString);
            if (escapedAndQuoted.length() >= 2) {
                return escapedAndQuoted.substring(1, escapedAndQuoted.length() - 1);
            }
            return "";
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON转义字符串时出错:" + originalString, e);
        }
    }

    /**
     * 根据传入的type获取对应的java类
     *
     * @param type
     */
    public static JavaType getJavaType(Type type) {
        // 判断是否带有泛型
        if (type instanceof ParameterizedType) {
            // 泛型集合
            Type[] actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
            // 获取原生的类型
            Class rowClass = (Class) ((ParameterizedType) type).getRawType();
            // 将原生类型有的所有的泛型存储到JavaType
            JavaType[] javaTypes = new JavaType[actualTypeArguments.length];
            for (int i = 0; i < actualTypeArguments.length; i++) {
                // 泛型也可能带有泛型，递归获取
                javaTypes[i] = getJavaType(actualTypeArguments[i]);
            }
            return TypeFactory.defaultInstance().constructParametricType(rowClass, javaTypes);
        } else {
            // 简单类型直接使用该类构建JavaType
            Class clazz = (Class) type;
            return TypeFactory.defaultInstance().constructParametricType(clazz, new JavaType[0]);
        }
    }

}