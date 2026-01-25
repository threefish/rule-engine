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

import org.springframework.http.HttpHeaders;

import java.util.*;

/**
 * @author 黄川 huchuc@vip.qq.com
 */
public final class HttpHeaderUtils {

    private HttpHeaderUtils() {
    }

    /**
     * 将 HttpHeaders 转换为 Map（Key 为 CamelCase，兼容多值 Header）
     *
     * 规则：
     * - 单值 Header -> String
     * - 多值 Header -> List<String>
     * - Key: Content-Type -> ContentType
     */
    public static Map<String, Object> toCamelCaseMap(HttpHeaders headers) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (headers == null || headers.isEmpty()) {
            return result;
        }

        headers.forEach((key, values) -> {
            if (values == null || values.isEmpty()) {
                return;
            }
            String camelKey = toCamelCaseHeaderKey(key);
            if (values.size() == 1) {
                result.put(camelKey, values.get(0));
            } else {
                result.put(camelKey, new ArrayList<>(values));
            }
        });

        return result;
    }

    /**
     * Header Key 转 CamelCase
     * Content-Type -> ContentType
     * X-Request-Id -> XRequestId
     */
    private static String toCamelCaseHeaderKey(String headerKey) {
        if (headerKey == null || headerKey.isEmpty()) {
            return headerKey;
        }

        StringBuilder sb = new StringBuilder(headerKey.length());
        boolean upperNext = true;

        for (char c : headerKey.toCharArray()) {
            if (c == '-' || c == '_') {
                upperNext = true;
                continue;
            }
            sb.append(upperNext ? Character.toUpperCase(c) : c);
            upperNext = false;
        }
        return sb.toString();
    }
}
