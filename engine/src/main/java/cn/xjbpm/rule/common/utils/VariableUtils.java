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

import cn.xjbpm.rule.engine.aviator.AviatorContext;
import cn.xjbpm.rule.engine.aviator.AviatorExecutor;

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

    /**
     * 设置字段值
     *
     * @param assignmentFiled
     * @param valueFiled
     * @param value
     * @param variable
     */
    public static void setPathVariable(String assignmentFiled, String valueFiled, Object value, Map<String, Object> variable) {
        String valueKey = valueFiled;
        variable.put(valueKey, value);
        AviatorContext aviatorContext = AviatorContext.builder().cached(true)
                .expression(String.format("%s=%s", assignmentFiled, valueKey))
                .env(variable)
                .build();
        AviatorExecutor.execute(aviatorContext);
        variable.remove(valueKey);
    }
}