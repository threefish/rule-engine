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
package cn.xjbpm.rule.utils;

import cn.xjbpm.rule.utils.lambda.SFunction;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;

import java.beans.Introspector;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 黄川 huchuc@vip.qq.com
 */
public class FieldUtil {
    private static final ConcurrentHashMap<SFunction<?, ?>, String> LAMBDA_CACHE = new ConcurrentHashMap<>();

    public static <T, R> String name(SFunction<T, R> fn) {
        return LAMBDA_CACHE.computeIfAbsent(fn, FieldUtil::doParseFieldName);
    }

    private static String doParseFieldName(SFunction<?, ?> fn) {
        try {
            Method method = fn.getClass().getDeclaredMethod("writeReplace");
            method.setAccessible(true);
            SerializedLambda serializedLambda = (SerializedLambda) method.invoke(fn);
            String implMethodName = serializedLambda.getImplMethodName();
            if (implMethodName.startsWith("get") || implMethodName.startsWith("set")) {
                String fieldName = implMethodName.substring(3);
                return Introspector.decapitalize(fieldName);
            }
            if (implMethodName.startsWith("is")) {
                String fieldName = implMethodName.substring(2);
                return Introspector.decapitalize(fieldName);
            }
            throw new IllegalArgumentException("Lambda 表达式非标准的 Getter Setter is 方法: " + implMethodName);
        } catch (Exception e) {
            throw new RuntimeException("解析 Lambda 字段名失败", e);
        }
    }

    public static <T, R> Expression<R> field(Root<T> root, SFunction<T, R> fn) {
        return root.get(LAMBDA_CACHE.computeIfAbsent(fn, FieldUtil::doParseFieldName));
    }
}