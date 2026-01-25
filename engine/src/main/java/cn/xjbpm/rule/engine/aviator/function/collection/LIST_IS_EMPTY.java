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
package cn.xjbpm.rule.engine.aviator.function.collection;

import cn.hutool.core.collection.CollUtil;
import cn.xjbpm.rule.engine.aviator.function.AbstractBaseFunction;
import cn.xjbpm.rule.engine.aviator.function.AviatorExtendFunction;
import com.googlecode.aviator.runtime.type.AviatorBoolean;
import com.googlecode.aviator.runtime.type.AviatorObject;
import org.springframework.util.Assert;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.List;

/**
 * 判断集合是否为空的函数
 * 支持 List, Set, Map, Array。严格执行非空校验。
 *
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/7/5
 */
@SuppressWarnings("all")
public class LIST_IS_EMPTY extends AbstractBaseFunction {

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        Object value = arg1.getValue(env);

        Assert.notNull(value, String.format("函数 %s 的参数不能为空", getName()));

        // 1. 处理 Collection (List, Set 等)
        if (value instanceof Collection) {
            return AviatorBoolean.valueOf(((Collection<?>) value).isEmpty());
        }

        // 2. 处理 Map
        if (value instanceof Map) {
            return AviatorBoolean.valueOf(((Map<?, ?>) value).isEmpty());
        }

        // 3. 处理 数组 (原生数组与对象数组)
        if (value.getClass().isArray()) {
            return AviatorBoolean.valueOf(Array.getLength(value) == 0);
        }

        // 4. 防呆：如果传入的既不是集合也不是数组，抛出参数类型错误异常
        throw new IllegalArgumentException(String.format("函数 %s 期待集合、Map或数组类型，实际类型为: %s",
                getName(), value.getClass().getName()));
    }

    @Override
    public List<AviatorExtendFunction> docs() {
        return Collections.singletonList(
                new AviatorExtendFunction(
                        getName(),
                        String.format("%s(collection)", getName()),
                        "boolean",
                        "判断集合、Map或数组是否为空。任意参数为 null 或非集合类型将抛出异常。",
                        String.format("%s(seq.list(1, 2))", getName())
                )
        );
    }
}