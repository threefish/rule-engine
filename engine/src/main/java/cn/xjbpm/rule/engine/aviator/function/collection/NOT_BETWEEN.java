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

import cn.xjbpm.rule.engine.aviator.function.AbstractBaseFunction;
import cn.xjbpm.rule.engine.aviator.function.AviatorExtendFunction;
import com.googlecode.aviator.runtime.type.AviatorBoolean;
import com.googlecode.aviator.runtime.type.AviatorObject;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 判断数值是否不在指定区间内的函数（包含边界检查）
 * 强化了防御性编程，支持数字及数字字符串，严格执行非空校验。
 *
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/7/5
 */
@SuppressWarnings("all")
public class NOT_BETWEEN extends AbstractBaseFunction {

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2, AviatorObject arg3) {
        Object value = arg1.getValue(env);
        Object startValue = arg2.getValue(env);
        Object endValue = arg3.getValue(env);

        Assert.notNull(value, String.format("函数 %s 的待比较值(var1)不能为空", getName()));
        Assert.notNull(startValue, String.format("函数 %s 的区间起始值(var2)不能为空", getName()));
        Assert.notNull(endValue, String.format("函数 %s 的区间结束值(var3)不能为空", getName()));

        try {
            // 尝试转换为 BigDecimal，支持 Number 类型、科学计数法及数字字符串
            BigDecimal v = toBigDecimal(value);
            BigDecimal start = toBigDecimal(startValue);
            BigDecimal end = toBigDecimal(endValue);

            // 逻辑优化：判断值是否不在 [start, end] 区间内
            // 如果 v < start 或者 v > end，则返回 true（表示不在区间内）
            boolean isNotBetween = v.compareTo(start) < 0 || v.compareTo(end) > 0;

            return AviatorBoolean.valueOf(isNotBetween);
        } catch (Exception e) {
            // 防呆：如果转换失败（非数字格式），抛出参数异常
            throw new IllegalArgumentException(String.format("函数 %s 参数转换数值失败，请确保输入为数字或数字字符串。错误信息：%s",
                    getName(), e.getMessage()));
        }
    }

    /**
     * 将 Object 安全转换为 BigDecimal
     */
    private BigDecimal toBigDecimal(Object obj) {
        if (obj instanceof BigDecimal) {
            return (BigDecimal) obj;
        }
        return new BigDecimal(obj.toString());
    }

    @Override
    public List<AviatorExtendFunction> docs() {
        return Collections.singletonList(
                new AviatorExtendFunction(
                        getName(),
                        String.format("%s(value, start, end)", getName()),
                        "boolean",
                        "判断数值是否不在 [start, end] 区间内。支持数字及数字字符串。任意参数为 null 或非数字格式将抛出异常。",
                        String.format("%s(12, 1, 10)", getName()))
        );
    }
}