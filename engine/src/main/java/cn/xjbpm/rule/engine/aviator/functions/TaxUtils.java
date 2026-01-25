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

/**
 * 税务工具类
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@FunctionNamespace(name = "tax")
@SuppressWarnings("all")
public class TaxUtils {

    @FunctionDoc(value = "tax.fastDeduction(var1)", description = "获取个人所得税速算扣除数。支持数字或数字字符串。参数为 null 将抛出异常。", example = "tax.fastDeduction(100000)")
    public static long fastDeduction(Object val) {
        Assert.notNull(val, "tax.fastDeduction -> 参数不能为空");

        long value = toLong(val);
        // 从最高阈值向下判断，确保逻辑严密
        if (value >= 960000) {
            return 181920L;
        } else if (value >= 660000) {
            return 85920L;
        } else if (value >= 420000) {
            return 52920L;
        } else if (value >= 300000) {
            return 31920L;
        } else if (value >= 144000) {
            return 16920L;
        } else if (value >= 36000) {
            return 2520L;
        }
        return 0L;
    }

    @FunctionDoc(value = "tax.getIncomeTaxRate(var1)", description = "获取个人所得税税率(%)。支持数字或数字字符串。参数为 null 将抛出异常。", example = "tax.getIncomeTaxRate(100000)")
    public static long getIncomeTaxRate(Object val) {
        Assert.notNull(val, "tax.getIncomeTaxRate -> 参数不能为空");

        long value = toLong(val);
        // 从最高阈值向下判断
        if (value >= 960000) {
            return 45L;
        } else if (value >= 660000) {
            return 35L;
        } else if (value >= 420000) {
            return 30L;
        } else if (value >= 300000) {
            return 25L;
        } else if (value >= 144000) {
            return 20L;
        } else if (value >= 36000) {
            return 10L;
        }
        return 3L;
    }

    /**
     * 内部通用转换方法：将 Object 安全转换为 long
     * 支持处理来自 Aviator 的 BigDecimal, Double, Long 或 String 类型
     */
    private static long toLong(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        try {
            // 处理字符串输入，先转为 BigDecimal 再取 longValue 可兼顾科学计数法或小数
            return new BigDecimal(obj.toString()).longValue();
        } catch (Exception e) {
            throw new IllegalArgumentException("无法将参数 [" + obj + "] 转换为有效的数字类型以便计算税务阈值", e);
        }
    }
}