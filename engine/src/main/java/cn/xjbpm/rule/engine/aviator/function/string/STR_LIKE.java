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

/**
 * 字符串 LIKE 匹配函数
 * 强化了防御性编程，支持 SQL 风格的通配符（% 和 _），严格执行参数非空校验。
 *
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/7/5
 */
@SuppressWarnings("all")
public class STR_LIKE extends AbstractBaseFunction {

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        Object valueObj = arg1.getValue(env);
        Object patternObj = arg2.getValue(env);


        Assert.notNull(valueObj, String.format("函数 %s 的目标字符串(var1)不能为空", getName()));
        Assert.notNull(patternObj, String.format("函数 %s 的匹配模式(var2)不能为空", getName()));

        String var1 = valueObj.toString();
        String var2 = patternObj.toString();

        // 将LIKE模式转换为正则表达式
        String regex = var2.replace("\\", "\\\\")
                .replace("?", ".")
                .replace("%", ".*");
        // 使用正则表达式匹配
        return AviatorBoolean.valueOf(var1.matches(regex));
    }

    @Override
    public List<AviatorExtendFunction> docs() {
        return Collections.singletonList(
                new AviatorExtendFunction(
                        getName(),
                        String.format("%s(var1, var2)", getName()),
                        "boolean",
                        "判断字符串是否匹配 LIKE 模式。支持 % (任意字符) 和 _ (单个字符)。参数为 null 将抛出异常。",
                        String.format("%s('hello world', 'hello%%')", getName()))
        );
    }
}