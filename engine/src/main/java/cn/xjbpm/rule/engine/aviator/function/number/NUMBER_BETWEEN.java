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

package cn.xjbpm.rule.engine.aviator.function.number;

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
 * 数值区间判断函数
 * 强化了防御性编程，判断数值是否在指定闭区间内，严格执行非空及数值格式校验。
 *
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/8/6
 */
@SuppressWarnings("all")
public class NUMBER_BETWEEN extends AbstractBaseFunction {

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2, AviatorObject arg3) {
        Object valueObj = arg1.getValue(env);
        Object startObj = arg2.getValue(env);
        Object endObj = arg3.getValue(env);


        Assert.notNull(valueObj, String.format("函数 %s 的待校验值(var1)不能为空", getName()));
        Assert.notNull(startObj, String.format("函数 %s 的区间起始值(var2)不能为空", getName()));
        Assert.notNull(endObj, String.format("函数 %s 的区间结束值(var3)不能为空", getName()));

        BigDecimal v = toBigDecimal(valueObj, "待校验值(var1)");
        BigDecimal start = toBigDecimal(startObj, "区间起始值(var2)");
        BigDecimal end = toBigDecimal(endObj, "区间结束值(var3)");

        // 比较值是否在区间内（包含边界 [start, end]）
        boolean result = v.compareTo(start) >= 0 && v.compareTo(end) <= 0;

        return AviatorBoolean.valueOf(result);
    }

    /**
     * 内部通用转换方法：将 Object 安全转换为 BigDecimal，失败时抛出异常
     */
    private BigDecimal toBigDecimal(Object obj, String paramName) {
        if (obj instanceof BigDecimal) {
            return (BigDecimal) obj;
        }
        try {
            return new BigDecimal(obj.toString().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("函数 %s 的参数 %s 内容 [%s] 无法转换为有效的数字类型",
                    getName(), paramName, obj));
        }
    }

    @Override
    public List<AviatorExtendFunction> docs() {
        return Collections.singletonList(
                new AviatorExtendFunction(
                        getName(),
                        String.format("%s(value, start, end)", getName()),
                        "boolean",
                        "判断数字是否在 [start, end] 区间内（包含边界）。支持数字与字符串混合。任意参数为 null 或非数字将抛出异常。",
                        String.format("%s(5, 1, 10)", getName()))
        );
    }
}