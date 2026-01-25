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

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.xjbpm.rule.engine.aviator.annotation.FunctionDoc;
import cn.xjbpm.rule.engine.aviator.annotation.FunctionNamespace;
import org.springframework.util.Assert;

import java.util.Date;

/**
 * 日期工具类
 * 支持 Date, String, Number 类型的混合输入，严格执行非空校验。
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@FunctionNamespace(name = "date")
@SuppressWarnings("all")
public class DateUtils {

    private static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @FunctionDoc(value = "date.now()", description = "获取当前时间 Date 对象。", example = "date.now()")
    public static Date now() {
        return new Date();
    }

    @FunctionDoc(value = "date.nowFormat('yyyy-MM-dd HH:mm:ss')", description = "获取当前时间字符串。format 为 null 将抛出异常。", example = "date.nowFormat('yyyy-MM-dd')")
    public static String nowFormat(Object format) {
        Assert.notNull(format, "date.nowFormat(format) -> format 不能为空");
        return DateUtil.format(new Date(), format.toString());
    }

    @FunctionDoc(value = "date.timestamp()", description = "获取当前时间戳 (long)。", example = "date.timestamp()")
    public static long timestamp() {
        return System.currentTimeMillis();
    }

    public static String format(Object date) {
        return format(date, DEFAULT_FORMAT);
    }

    @FunctionDoc(value = "date.format(date,format)", description = "日期格式化。支持 Date/String/Number 输入。date 为 null 将抛出异常。", example = "date.format(date.now(), 'yyyy-MM-dd')")
    public static String format(Object date, Object format) {
        Assert.notNull(date, "date.format(date, format) -> date 不能为空");
        Assert.notNull(format, "date.format(date, format) -> format 不能为空");
        return DateUtil.format(toDate(date), format.toString());
    }

    /**
     * 内部通用转换方法：将 Object (Date/String/Number) 安全转换为 Date
     */
    private static Date toDate(Object obj) {
        if (obj instanceof Date) {
            return (Date) obj;
        }
        if (obj instanceof Number) {
            return new Date(((Number) obj).longValue());
        }
        if (obj instanceof CharSequence) {
            return DateUtil.parse(obj.toString());
        }
        // 如果无法识别，尝试通过 toString 强制解析，解析失败 Hutool 会抛出异常
        return DateUtil.parse(obj.toString());
    }

    @FunctionDoc(value = "date.parse(var1,var2)", description = "日期解析并返回标准格式字符串。任意参数为 null 将抛出异常。", example = "date.parse('2023-01-01', 'yyyy-MM-dd')")
    public static String parse(Object date, Object format) {
        Assert.notNull(date, "date.parse(date, format) -> date 不能为空");
        Assert.notNull(format, "date.parse(date, format) -> format 不能为空");
        Date d = DateUtil.parse(date.toString(), format.toString());
        return DateUtil.format(d, DEFAULT_FORMAT);
    }

    @FunctionDoc(value = "date.sub(var1,var2,var3)", description = "日期减少指定天数。支持混合类型输入。任意参数为 null 将抛出异常。", example = "date.sub('2023-01-01', 'yyyy-MM-dd', 1)")
    public static String sub(Object date, Object format, Object amount) {
        Assert.notNull(amount, "date.sub -> amount 不能为空");
        int val = Integer.parseInt(amount.toString());
        return add(date, format, -val);
    }

    @FunctionDoc(value = "date.add(var1,var2,var3)", description = "日期增加指定天数。支持混合类型输入。任意参数为 null 将抛出异常。", example = "date.add('2023-01-01', 'yyyy-MM-dd', 1)")
    public static String add(Object date, Object format, Object amount) {
        Assert.notNull(date, "date.add -> date 不能为空");
        Assert.notNull(format, "date.add -> format 不能为空");
        Assert.notNull(amount, "date.add -> amount 不能为空");
        Date d = DateUtil.parse(date.toString(), format.toString());
        int val = Integer.parseInt(amount.toString());
        return DateUtil.format(DateUtil.offset(d, DateField.DAY_OF_YEAR, val), DEFAULT_FORMAT);
    }

    @FunctionDoc(value = "date.diffDays(var1,var2,var3)", description = "计算两个日期间隔天数（指定解析格式）。var1 var2 任一参数为 null 将抛出异常。", example = "date.diffDays('2023-01-01', '2023-01-10', 'yyyy-MM-dd')")
    public static long diffDays(Object startDate, Object endDate, Object format) {
        Assert.notNull(startDate, "date.diffDays -> startDate 不能为空");
        Assert.notNull(endDate, "date.diffDays -> endDate 不能为空");
        Assert.notNull(format, "date.diffDays -> format 不能为空");
        Date d1 = DateUtil.parse(startDate.toString(), format.toString());
        Date d2 = DateUtil.parse(endDate.toString(), format.toString());
        return DateUtil.between(d1, d2, DateUnit.DAY);
    }

    public static long diffDays(Object startDate, Object endDate) {
        Assert.notNull(startDate, "date.diffDays -> startDate 不能为空");
        Assert.notNull(endDate, "date.diffDays -> endDate 不能为空");
        return DateUtil.between(toDate(startDate), toDate(endDate), DateUnit.DAY);
    }

    @FunctionDoc(value = "date.addDays(var1,var2)", description = "日期增加天数（自动解析）。参数为 null 将抛出异常。", example = "date.addDays(date.now(), 5)")
    public static String addDays(Object date, Object amount) {
        Assert.notNull(date, "date.addDays -> date 不能为空");
        Assert.notNull(amount, "date.addDays -> amount 不能为空");
        int val = Integer.parseInt(amount.toString());
        return DateUtil.format(DateUtil.offset(toDate(date), DateField.DAY_OF_YEAR, val), DEFAULT_FORMAT);
    }

    @FunctionDoc(value = "date.isBefore(var1,var2,var3)", description = "判断 date1 是否在 date2 之前。任意参数为 null 将抛出异常。", example = "date.isBefore('2023-01-01', '2023-01-02', 'yyyy-MM-dd')")
    public static boolean isBefore(Object date1, Object date2, Object format) {
        Assert.notNull(date1, "date.isBefore -> date1 不能为空");
        Assert.notNull(date2, "date.isBefore -> date2 不能为空");
        Assert.notNull(format, "date.isBefore -> format 不能为空");
        String f = format.toString();
        return DateUtil.parse(date1.toString(), f).before(DateUtil.parse(date2.toString(), f));
    }

    @FunctionDoc(value = "date.isAfter(var1,var2,var3)", description = "判断 date1 是否在 date2 之后。任意参数为 null 将抛出异常。", example = "date.isAfter('2023-01-02', '2023-01-01', 'yyyy-MM-dd')")
    public static boolean isAfter(Object date1, Object date2, Object format) {
        Assert.notNull(date1, "date.isAfter -> date1 不能为空");
        Assert.notNull(date2, "date.isAfter -> date2 不能为空");
        Assert.notNull(format, "date.isAfter -> format 不能为空");
        String f = format.toString();
        return DateUtil.parse(date1.toString(), f).after(DateUtil.parse(date2.toString(), f));
    }

    @FunctionDoc(value = "date.isToday(var1)", description = "判断是否为今天。参数为 null 将抛出异常。", example = "date.isToday('2023-01-01')")
    public static boolean isToday(Object date) {
        Assert.notNull(date, "date.isToday -> date 不能为空");
        return DateUtil.isSameDay(toDate(date), new Date());
    }

    @FunctionDoc(value = "date.isSameDay(var1,var2)", description = "判断是否为同一天。参数为 null 将抛出异常。", example = "date.isSameDay(date.now(), '2023-01-01')")
    public static boolean isSameDay(Object date1, Object date2) {
        Assert.notNull(date1, "date.isSameDay -> date1 不能为空");
        Assert.notNull(date2, "date.isSameDay -> date2 不能为空");
        return DateUtil.isSameDay(toDate(date1), toDate(date2));
    }
}