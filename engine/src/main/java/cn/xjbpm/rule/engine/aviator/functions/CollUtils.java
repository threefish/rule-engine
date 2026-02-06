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

package cn.xjbpm.rule.engine.aviator.functions;


import cn.xjbpm.rule.engine.aviator.annotation.FunctionDoc;
import cn.xjbpm.rule.engine.aviator.annotation.FunctionNamespace;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 */
@FunctionNamespace(name = "coll")
@SuppressWarnings("all")
public class CollUtils {

    @FunctionDoc(value = "coll.contains(var1,var2)", description = "集合包含，支持Collection、数组、Map", example = "coll.contains(seq.list(1,2,3),4)")
    public static boolean contains(Object var1, Object var2) {
        Assert.notNull(var1, "coll.contains(var1,var2)  var1参数不能为null");
        Assert.notNull(var2, "coll.contains(var1,var2)  var2参数不能为null");
        if (var1 instanceof Collection) {
            Collection collection = (Collection) var1;
            return collection.contains(var2);
        }
        // 如果是数组
        if (var1.getClass().isArray()) {
            Object[] array = (Object[]) var1;
            return Arrays.asList(array).contains(var2);
        }
        // 扩展支持Map类型
        if (var1 instanceof Map) {
            Map map = (Map) var1;
            return map.containsKey(var2);
        }
        throw new IllegalArgumentException("coll.contains(var1,var2)  var1参数只能是Collection、数组、Map");
    }

    @FunctionDoc(value = "coll.isEmpty(var1)", description = "集合是否为空，支持Collection、数组、Map", example = "coll.isEmpty(seq.list(1,2,3))")
    public static boolean isEmpty(Object var1) {
        Assert.notNull(var1, "coll.isEmpty(var1)  var1参数不能为null");
        if (var1 instanceof Collection) {
            Collection collection = (Collection) var1;
            return collection.isEmpty();
        }
        // 如果是数组
        if (var1.getClass().isArray()) {
            Object[] array = (Object[]) var1;
            return array.length == 0;
        }
        // 扩展支持Map类型
        if (var1 instanceof Map) {
            Map map = (Map) var1;
            return map.isEmpty();
        }
        throw new IllegalArgumentException("coll.isEmpty(var1)  var1参数只能是Collection、数组、Map");
    }

    @FunctionDoc(value = "coll.size(var1)", description = "集合大小，支持Collection、数组、Map和字符串", example = "coll.size(seq.list(1,2,3))")
    public static int size(Object var1) {
        Assert.notNull(var1, "coll.size(var1)  var1参数不能为null");
        if (var1 instanceof Collection) {
            Collection collection = (Collection) var1;
            return collection.size();
        }
        // 如果是数组
        if (var1.getClass().isArray()) {
            Object[] array = (Object[]) var1;
            return array.length;
        }
        // 扩展支持Map类型
        if (var1 instanceof Map) {
            Map map = (Map) var1;
            return map.size();
        }
        throw new IllegalArgumentException("coll.size(var1)  var1参数只能是Collection、数组、Map");
    }

}