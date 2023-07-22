package cn.xjbpm.rule.engine.common.utils;

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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author 黄川 huchuc@vip.qq.com
 */
@UtilityClass
public class JsonUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new Jdk8Module())
            .registerModule(new JavaTimeModule()).setSerializationInclusion(JsonInclude.Include.NON_NULL)
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
