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

package cn.xjbpm.rule.engine.aviator.function.string;

import cn.xjbpm.rule.engine.aviator.function.AbstractBaseFunction;
import cn.xjbpm.rule.engine.aviator.function.AviatorExtendFunction;
import com.googlecode.aviator.runtime.type.AviatorBoolean;
import com.googlecode.aviator.runtime.type.AviatorObject;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 对象相等判断函数
 * 强化了防御性编程，判断两个对象是否相等，严格执行参数非空校验。
 *
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/7/5
 */
@SuppressWarnings("all")
public class EQUALS extends AbstractBaseFunction {

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        Object var1 = arg1.getValue(env);
        Object var2 = arg2.getValue(env);


        Assert.notNull(var1, String.format("函数 %s 的比较对象(var1)不能为空", getName()));
        Assert.notNull(var2, String.format("函数 %s 的比较对象(var2)不能为空", getName()));

        // 执行相等性比较
        return AviatorBoolean.valueOf(Objects.equals(var1, var2));
    }

    @Override
    public List<AviatorExtendFunction> docs() {
        return Collections.singletonList(
                new AviatorExtendFunction(
                        getName(),
                        String.format("%s(var1, var2)", getName()),
                        "boolean",
                        "判断两个对象是否相等。支持多种类型。任意参数为 null 将抛出异常。",
                        String.format("%s('hello', 'hello')", getName()))
        );
    }
}