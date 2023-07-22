package cn.xjbpm.rule.engine.common.utils;

import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/6/30
 */
public class VariableUtils {

    public static Object getByPathVariable(String path, Map<String, Object> variable) {
        String[] split = path.split("\\.");
        Map<String, Object> current = variable;
        for (String key : split) {
            Object object = current.get(key);
            if (object instanceof Map) {
                current = (Map<String, Object>) current.get(key);
            } else {
                return object;
            }
        }
        return current;
    }
}
