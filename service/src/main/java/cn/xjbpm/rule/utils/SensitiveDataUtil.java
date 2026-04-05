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


package cn.xjbpm.rule.utils;

import java.util.*;

/**
 * 敏感数据处理工具类
 *
 * @author 黄川 huchuc@vip.qq.com
 */
public class SensitiveDataUtil {
    /**
     * 敏感字段（小写）
     */
    private static final Set<String> SENSITIVE_KEYS = new HashSet<>(Arrays.asList(
            "password", "passphrase", "privatekey",
            "secret", "token", "apikey", "credential",
            "username", "key", "value", "host",
            "apiv3key", "accesstoken", "merchantserialnumber"
    ));

    private static final String MASK_PATTERN = "******";

    /**
     * 对 Map 中的敏感信息进行脱敏处理
     *
     * @param authDataMap 原始数据
     * @return 脱敏后的新 Map
     */
    public static Map<String, Object> maskSensitiveData(Map<String, Object> authDataMap) {
        if (authDataMap == null) {
            return null;
        }

        Map<String, Object> maskedMap = new LinkedHashMap<>();

        authDataMap.forEach((key, value) -> {
            if (key != null && isSensitiveKey(key)) {
                maskedMap.put(key, maskValue(value));
            } else {
                maskedMap.put(key, value);
            }
        });

        return maskedMap;
    }

    /**
     * 判断 Key 是否属于敏感字段
     */
    private static boolean isSensitiveKey(String key) {
        return SENSITIVE_KEYS.contains(key.toLowerCase());
    }

    /**
     * 脱敏逻辑：根据长度动态调整保留位数
     * 策略：
     * 1. 长度 <= 2: 全掩码
     * 2. 长度 <= 6: 保留首尾各 1 位
     * 3. 长度 > 6:  保留前 1/4 和 后 1/4，中间使用固定 6 个星号
     */
    private static Object maskValue(Object value) {
        if (value == null) {
            return null;
        }
        String strValue = String.valueOf(value);
        int len = strValue.length();

        if (len <= 2) {
            return MASK_PATTERN;
        }

        if (len <= 6) {
            return strValue.charAt(0) + MASK_PATTERN + strValue.charAt(len - 1);
        }

        int keep = Math.min(len / 4, 8);
        if (keep < 2) {
            keep = 2;
        }

        return strValue.substring(0, keep) + MASK_PATTERN + strValue.substring(len - keep);
    }

    /**
     * 判断值是否为脱敏后的值
     *
     * @param value 待检测的值
     * @return 是否为脱敏值
     */
    public static boolean isMaskedValue(Object value) {
        if (value == null) {
            return false;
        }
        String strValue = String.valueOf(value);
        return strValue.contains(MASK_PATTERN);
    }

    /**
     * 智能合并新旧数据
     * 对于敏感字段，如果新值是脱敏值则保留旧值，否则使用新值
     * 对于非敏感字段，直接使用新值
     *
     * @param oldData 旧数据（原始明文）
     * @param newData 新数据（可能包含脱敏值）
     * @return 合并后的数据
     */
    public static Map<String, Object> mergeAuthData(Map<String, Object> oldData, Map<String, Object> newData) {
        if (oldData == null && newData == null) {
            return new LinkedHashMap<>();
        }
        if (oldData == null) {
            return new LinkedHashMap<>(newData);
        }
        if (newData == null) {
            return new LinkedHashMap<>(oldData);
        }

        Map<String, Object> result = new LinkedHashMap<>();

        Set<String> allKeys = new LinkedHashSet<>();
        allKeys.addAll(oldData.keySet());
        allKeys.addAll(newData.keySet());

        for (String key : allKeys) {
            Object oldValue = oldData.get(key);
            Object newValue = newData.get(key);

            if (isSensitiveKey(key)) {
                if (newValue == null) {
                    result.put(key, null);
                } else if (isMaskedValue(newValue)) {
                    result.put(key, oldValue);
                } else {
                    result.put(key, newValue);
                }
            } else {
                result.put(key, newValue != null ? newValue : oldValue);
            }
        }
        return result;
    }
}
