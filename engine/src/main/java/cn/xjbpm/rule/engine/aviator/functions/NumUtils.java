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
package cn.xjbpm.rule.engine.aviator.functions;

import cn.xjbpm.rule.engine.aviator.annotation.FunctionDoc;
import cn.xjbpm.rule.engine.aviator.annotation.FunctionNamespace;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.BiFunction;

/**
 * 数字工具类
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@FunctionNamespace(name = "num")
@SuppressWarnings("all")
public class NumUtils {

    @FunctionDoc(value = "num.notBetween(var1,var2,var3)", description = "判断数字 var1 是否不在 [var2, var3] 区间内。支持数字与字符串混合。任意参数为 null 将抛出异常。", example = "num.notBetween(1,0,2)")
    public static boolean notBetween(Object var1, Object var2, Object var3) {
        return !between(var1, var2, var3);
    }

    @FunctionDoc(value = "num.between(var1,var2,var3)", description = "判断数字 var1 是否在 [var2, var3] 区间内。支持数字与字符串混合。任意参数为 null 将抛出异常。", example = "num.between(1,0,2)")
    public static boolean between(Object var1, Object var2, Object var3) {
        Assert.notNull(var1, "num.between -> var1 不能为空");
        Assert.notNull(var2, "num.between -> var2 不能为空");
        Assert.notNull(var3, "num.between -> var3 不能为空");

        BigDecimal b1 = toBigDecimal(var1);
        BigDecimal b2 = toBigDecimal(var2);
        BigDecimal b3 = toBigDecimal(var3);
        return b1.compareTo(b2) >= 0 && b1.compareTo(b3) <= 0;
    }

    /**
     * 内部通用转换方法：将 Object 安全转换为 BigDecimal
     */
    private static BigDecimal toBigDecimal(Object obj) {
        if (obj instanceof BigDecimal) {
            return (BigDecimal) obj;
        }
        try {
            return new BigDecimal(obj.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("无法将参数 [" + obj + "] 转换为有效的数字类型", e);
        }
    }

    @FunctionDoc(value = "num.add(var1,var2)", description = "数字相加。支持数字与字符串混合。任意参数为 null 将抛出异常。", example = "num.add(1, 2.5)")
    public static BigDecimal add(Object var1, Object var2) {
        return calculate(var1, var2, BigDecimal::add);
    }

    /**
     * 内部计算方法，处理通用二元运算
     */
    private static BigDecimal calculate(Object var1, Object var2, BiFunction<BigDecimal, BigDecimal, BigDecimal> operation) {
        Assert.notNull(var1, "数字运算操作数 var1 不能为空");
        Assert.notNull(var2, "数字运算操作数 var2 不能为空");
        return operation.apply(toBigDecimal(var1), toBigDecimal(var2));
    }

    @FunctionDoc(value = "num.sub(var1,var2)", description = "数字相减。支持数字与字符串混合。任意参数为 null 将抛出异常。", example = "num.sub(3, 1)")
    public static BigDecimal sub(Object var1, Object var2) {
        return calculate(var1, var2, BigDecimal::subtract);
    }

    @FunctionDoc(value = "num.mul(var1,var2)", description = "数字相乘。支持数字与字符串混合。任意参数为 null 将抛出异常。", example = "num.mul(2, 3)")
    public static BigDecimal mul(Object var1, Object var2) {
        return calculate(var1, var2, BigDecimal::multiply);
    }

    @FunctionDoc(value = "num.div(var1,var2)", description = "数字相除。默认保留10位小数并四舍五入。除以0将抛出异常。任意参数为 null 将抛出异常。", example = "num.div(6, 3)")
    public static BigDecimal div(Object var1, Object var2) {
        Assert.notNull(var1, "num.div -> var1 不能为空");
        Assert.notNull(var2, "num.div -> var2 不能为空");
        BigDecimal b1 = toBigDecimal(var1);
        BigDecimal b2 = toBigDecimal(var2);
        // 防呆：除数不能为0
        if (b2.compareTo(BigDecimal.ZERO) == 0) {
            throw new ArithmeticException("num.div -> 除数不能为 0");
        }
        return b1.divide(b2, 10, RoundingMode.HALF_UP).stripTrailingZeros();
    }

    @FunctionDoc(value = "num.abs(var1)", description = "获取数字绝对值。参数为 null 将抛出异常。", example = "num.abs(-5)")
    public static BigDecimal abs(Object var1) {
        Assert.notNull(var1, "num.abs -> var1 不能为空");
        return toBigDecimal(var1).abs();
    }

    @FunctionDoc(value = "num.round(var1,var2)", description = "数字四舍五入。var2 为保留小数位数。任意参数为 null 将抛出异常。", example = "num.round(2.556, 2)")
    public static BigDecimal round(Object var1, Object var2) {
        Assert.notNull(var1, "num.round -> var1 不能为空");
        Assert.notNull(var2, "num.round -> var2 不能为空");
        int scale = Integer.parseInt(var2.toString());
        return toBigDecimal(var1).setScale(scale, RoundingMode.HALF_UP);
    }

    @FunctionDoc(value = "num.round(var1)", description = "数字四舍五入。任意参数为 null 将抛出异常。", example = "num.round(2.556)")
    public static BigDecimal round(Object var1) {
        Assert.notNull(var1, "num.round -> var1 不能为空");
        return toBigDecimal(var1).setScale(0, RoundingMode.HALF_UP);
    }

    @FunctionDoc(value = "num.max(var1,var2)", description = "获取最大值。任意参数为 null 将抛出异常。", example = "num.max(3, 5)")
    public static BigDecimal max(Object var1, Object var2) {
        Assert.notNull(var1, "num.max -> var1 不能为空");
        Assert.notNull(var2, "num.max -> var2 不能为空");
        return toBigDecimal(var1).max(toBigDecimal(var2));
    }

    @FunctionDoc(value = "num.min(var1,var2)", description = "获取最小值。任意参数为 null 将抛出异常。", example = "num.min(3, 5)")
    public static BigDecimal min(Object var1, Object var2) {
        Assert.notNull(var1, "num.min -> var1 不能为空");
        Assert.notNull(var2, "num.min -> var2 不能为空");
        return toBigDecimal(var1).min(toBigDecimal(var2));
    }

    @FunctionDoc(value = "num.isOdd(var1)", description = "判断是否为奇数。参数为 null 将抛出异常。", example = "num.isOdd(3)")
    public static boolean isOdd(Object var1) {
        Assert.notNull(var1, "num.isOdd -> var1 不能为空");
        return !isEven(var1);
    }

    @FunctionDoc(value = "num.isEven(var1)", description = "判断是否为偶数。参数为 null 将抛出异常。", example = "num.isEven(2)")
    public static boolean isEven(Object var1) {
        Assert.notNull(var1, "num.isEven -> var1 不能为空");
        BigDecimal b = toBigDecimal(var1);
        return b.remainder(new BigDecimal("2")).compareTo(BigDecimal.ZERO) == 0;
    }
}