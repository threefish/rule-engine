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
package cn.xjbpm.rule.engine.aviator;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/6/30
 */
@Builder
@Data
public class AviatorContext implements Serializable {
    /**
     * 表达式
     */
    private String expression;
    /**
     * 表达式参数
     */
    private Map<String, Object> env;
    /**
     * 是否缓存
     */
    private boolean cached;


    public static AviatorContext create(String expression, Map<String, Object> env) {
        return AviatorContext.builder().expression(expression).cached(true).env(new SmartEnvMap(env)).build();
    }


    private static class SmartEnvMap extends HashMap<String, Object> {
        public SmartEnvMap(Map<String, Object> map) {
            if (map != null) {
                this.putAll(map);
            }
        }

        @Override
        public Object get(Object key) {
            Object val = super.get(key);
            // 如果找不到带 $ 的 key，自动尝试去掉 $
            if (val == null && key instanceof String) {
                String sKey = (String) key;
                if (sKey.startsWith("$")) {
                    return super.get(sKey.substring(1));
                }
            }
            return val;
        }

        @Override
        public boolean containsKey(Object key) {
            if (super.containsKey(key)) {
                return true;
            }
            if (key instanceof String) {
                String sKey = (String) key;
                if (sKey.startsWith("$")) {
                    return super.containsKey(sKey.substring(1));
                }
            }
            return false;
        }
    }
}