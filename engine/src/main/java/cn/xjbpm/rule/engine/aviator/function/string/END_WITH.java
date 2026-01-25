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
package cn.xjbpm.rule.engine.aviator.function.string;

import cn.xjbpm.rule.engine.aviator.function.AbstractBaseFunction;
import cn.xjbpm.rule.engine.aviator.function.AviatorExtendFunction;
import com.googlecode.aviator.runtime.type.AviatorBoolean;
import com.googlecode.aviator.runtime.type.AviatorObject;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 字符串结尾匹配函数
 * 强化了防御性编程，判断字符串是否以指定后缀结尾，严格执行参数非空校验。
 *
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/7/5
 */
@SuppressWarnings("all")
public class END_WITH extends AbstractBaseFunction {

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        Object value1 = arg1.getValue(env);
        Object value2 = arg2.getValue(env);

        
        Assert.notNull(value1, String.format("函数 %s 的目标字符串(var1)不能为空", getName()));
        Assert.notNull(value2, String.format("函数 %s 的匹配后缀(var2)不能为空", getName()));

        String var1 = value1.toString();
        String var2 = value2.toString();

        // 如果后缀为空字符串，按照标准逻辑返回 true
        if (var2.isEmpty()) {
            return AviatorBoolean.TRUE;
        }

        return AviatorBoolean.valueOf(var1.endsWith(var2));
    }

    @Override
    public List<AviatorExtendFunction> docs() {
        return Collections.singletonList(
                new AviatorExtendFunction(
                        getName(),
                        String.format("%s(var1, var2)", getName()),
                        "boolean",
                        "判断字符串 var1 是否以 var2 结尾。任意参数为 null 将抛出异常。",
                        String.format("%s('hello world', 'world')", getName()))
        );
    }
}