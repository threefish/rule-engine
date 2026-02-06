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
 * @author 黄川 huchuc@vip.qq.com
 */
public class SensitiveDataUtil {

    private static final Set<String> SENSITIVE_KEYS = new HashSet<>(Arrays.asList(
            "password", "passphrase", "privatekey", "secret", "token", "apikey", "credential", "username", "key", "value", "host"
    ));

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

        // 使用 LinkedHashMap 保持原始插入顺序
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
        return SENSITIVE_KEYS.contains(key.toLowerCase().replaceAll("[_-]", ""));
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
            return "******";
        }

        if (len <= 6) {
            // 保留首尾各 1 位，中间 4 个星号
            return strValue.charAt(0) + "****" + strValue.charAt(len - 1);
        }

        // 计算保留比例：前后各保留约 25% 的长度，但最多不超过 8 位（防止暴露过多）
        int keep = Math.min(len / 4, 8);
        if (keep < 2) {
            keep = 2; // 至少保留 2 位
        }

        String sb = strValue.substring(0, keep) + // 保留头部
                "******" +                    // 中间固定 6 个星号，隐藏真实长度
                strValue.substring(len - keep); // 保留尾部

        return sb;
    }
}