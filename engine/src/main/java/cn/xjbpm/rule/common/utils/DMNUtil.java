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
package cn.xjbpm.rule.common.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2026/2/6
 */
@SuppressWarnings("all")
public class DMNUtil {

    // 预编译正则：支持下划线的数值区间
    // 匹配 [1_000..2_000], (1..5), ]1..5[ 等
    // Group 1: Start ([ or ( or ])
    // Group 2: Min value (supports - and _)
    // Group 4: Max value (supports - and _)
    // Group 6: End (] or ) or [)
    private static final Pattern NUM_RANGE_PATTERN = Pattern.compile("^([\\[\\]\\(])\\s*(-?[\\d_]+(\\.[\\d_]+)?)\\.\\.(-?[\\d_]+(\\.[\\d_]+)?)\\s*([\\]\\[\\)])$");
    // 日期区间正则，匹配 [date("2026-02-05")..date("2026-02-06")]
    private static final Pattern DATE_RANGE_PATTERN = Pattern.compile("^([\\[\\]\\(])\\s*(date\\(\"[^\"]+\"\\))\\.\\.(date\\(\"[^\"]+\"\\))\\s*([\\]\\[\\)])$");


    /**
     * 将 DMN 表达式转换为 Aviator 表达式
     */

    public static String convertToAviator(String expr) {
        expr = expr.trim();
        // DMN 中 "-" 或空表示忽略此条件 (Always True)
        if (StringUtils.isBlank(expr) || "-".equals(expr.trim())) {
            return "";
        }
        // 1. DMN 规范允许一元测试以 = 开头（例如 = 100），这在表达式中是冗余的，先去掉它
        if (expr.startsWith("=") && !expr.startsWith("==")) {
            expr = expr.substring(1).trim();
        }
        // 1. 处理日期区间 [date("...")..date("...")]
        Matcher dateRangeMatcher = DATE_RANGE_PATTERN.matcher(expr);
        if (dateRangeMatcher.matches()) {
            String leftOp = "[".equals(dateRangeMatcher.group(1)) ? ">=" : ">";
            String rightOp = "]".equals(dateRangeMatcher.group(4)) ? "<=" : "<";
            String minDate = transformDate(dateRangeMatcher.group(2));
            String maxDate = transformDate(dateRangeMatcher.group(3));
            return String.format("x %s %s && x %s %s", leftOp, minDate, rightOp, maxDate);
        }

        // 2. 处理数字区间 [100..200]
        Matcher numRangeMatcher = NUM_RANGE_PATTERN.matcher(expr);
        if (numRangeMatcher.matches()) {
            String leftOp = "[".equals(numRangeMatcher.group(1)) ? ">=" : ">";
            String rightOp = "]".equals(numRangeMatcher.group(6)) ? "<=" : "<";
            String min = numRangeMatcher.group(2).replace("_", "");
            String max = numRangeMatcher.group(4).replace("_", "");
            return String.format("x %s %s && x %s %s", leftOp, min, rightOp, max);
        }

        // 3. 处理 not 逻辑 (支持 not("123", "222") 或 not(date("...")))
        if (expr.startsWith("not(") && expr.endsWith(")")) {
            String inner = expr.substring(4, expr.length() - 1).trim();
            // 如果内部包含多个值（逗号分隔且在引号外）
            if (isList(inner)) {
                return "!include(seq.list(" + transformAllDates(inner) + "), x)";
            }
            return "x != " + transformDate(inner);
        }

        // 4. 处理隐式列表 "123", "222" (匹配任意值 in)
        if (isList(expr)) {
            return "include(seq.list(" + transformAllDates(expr) + "), x)";
        }

        // 5. 处理带操作符的日期比较: < date("..."), > date("...")
        if (expr.startsWith("<") || expr.startsWith(">") || expr.startsWith("!=")) {
            // 提取操作符和内容
            String op = expr.startsWith(">=") || expr.startsWith("<=") ? expr.substring(0, 2) : expr.substring(0, 1);
            String content = expr.substring(op.length()).trim();
            return "x " + op + " " + transformDate(content);
        }

        // 6. 处理精确等于日期: date("2026-02-05")
        if (expr.startsWith("date(")) {
            return "x == " + transformDate(expr);
        }

        // 7. 基础字面量处理
        if (expr.startsWith("\"")) {
            return "x == " + expr;
        }
        if (expr.matches("-?[\\d_]+(\\.[\\d_]+)?")) {
            return "x == " + expr.replace("_", "");
        }


        return "x == " + expr;
    }


    /**
     * 判断是否为逗号分隔的列表
     */
    private static boolean isList(String inner) {
        // 简单逻辑：包含引号间的逗号 "A","B"
        return inner.contains("\",\"") || (inner.contains(",") && !inner.contains("date("));
    }

    /**
     * 将 DMN date("2026-01-01") 转换为 Aviator 内部日期函数
     * Aviator 默认可以使用 identity(seq.date("yyyy-MM-dd"))
     */
    private static String transformDate(String part) {
        if (part.startsWith("date(\"") && part.endsWith("\")")) {
            String dateStr = part.substring(6, part.length() - 2);
            // 转换为 Aviator 的日期构造方式
            return "date.format('" + dateStr + "', 'yyyy-MM-dd')";
        }
        return part;
    }

    /**
     * 批量转换列表中的日期字面量
     */
    private static String transformAllDates(String expr) {
        // 简单的正则替换，将所有的 date("...") 替换为 sysdate(...)
        return expr.replaceAll("date\\(\"([^\"]+)\"\\)", "date.format('$1', 'yyyy-MM-dd')");
    }

}