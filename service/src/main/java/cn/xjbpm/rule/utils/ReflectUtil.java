/**
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

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2026/3/13
 */
public class ReflectUtil {


    /**
     * 反射执行无参方法
     */
    public static void reflectInvokeMethod(Object instance, String methodName) throws Exception {
        java.lang.reflect.Method method = instance.getClass().getDeclaredMethod(methodName);
        method.setAccessible(true);
        method.invoke(instance);
    }

    /**
     * 反射获取字段
     */
    public static Object reflectGetField(Object instance, String fieldName) throws Exception {
        java.lang.reflect.Field field = instance.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(instance);
    }

    /**
     * 反射设置字段
     */
    public static void reflectSetField(Object instance, String fieldName, Object value) throws Exception {
        java.lang.reflect.Field field = instance.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(instance, value);
    }
}