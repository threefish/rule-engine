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

package cn.xjbpm.rule.engine.aviator.functions;

import cn.hutool.core.util.StrUtil;
import cn.xjbpm.rule.common.utils.MarkdownUtils;
import cn.xjbpm.rule.engine.aviator.annotation.FunctionDoc;
import cn.xjbpm.rule.engine.aviator.annotation.FunctionNamespace;
import org.springframework.util.Assert;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串工具类
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@FunctionNamespace(name = "str")
@SuppressWarnings("all")
public class StringUtils {

    @FunctionDoc(value = "str.equals(var1,var2)", description = "字符串内容比较。任意参数为 null 将抛出异常。", example = "str.equals('hello','hello')")
    public static boolean equals(Object var1, Object var2) {
        Assert.notNull(var1, "str.equals -> var1 不能为空");
        Assert.notNull(var2, "str.equals -> var2 不能为空");
        return Objects.equals(var1.toString(), var2.toString());
    }


    @FunctionDoc(value = "str.replace(var1,old,new)", description = "字符串替换。任意参数为 null 将抛出异常。", example = "str.replace('hello','e','a')")
    public static String replace(Object str, Object oldStr, Object newStr) {
        Assert.notNull(str, "str.replace -> str 不能为空");
        Assert.notNull(oldStr, "str.replace -> oldStr 不能为空");
        Assert.notNull(newStr, "str.replace -> newStr 不能为空");
        return str.toString().replace(oldStr.toString(), newStr.toString());
    }

    @FunctionDoc(value = "str.replaceAll(var1,regex,new)", description = "字符串正则全量替换。任意参数为 null 将抛出异常。", example = "str.replaceAll('a1b2','\\\\d','-')")
    public static String replaceAll(Object str, Object regex, Object newStr) {
        Assert.notNull(str, "str.replaceAll -> str 不能为空");
        Assert.notNull(regex, "str.replaceAll -> regex 不能为空");
        Assert.notNull(newStr, "str.replaceAll -> newStr 不能为空");
        return str.toString().replaceAll(regex.toString(), newStr.toString());
    }

    @FunctionDoc(value = "str.replaceFirst(var1,regex,new)", description = "字符串正则替换首个匹配项。任意参数为 null 将抛出异常。", example = "str.replaceFirst('a1b2','\\\\d','-')")
    public static String replaceFirst(Object str, Object regex, Object newStr) {
        Assert.notNull(str, "str.replaceFirst -> str 不能为空");
        Assert.notNull(regex, "str.replaceFirst -> regex 不能为空");
        Assert.notNull(newStr, "str.replaceFirst -> newStr 不能为空");
        return str.toString().replaceFirst(regex.toString(), newStr.toString());
    }

    @FunctionDoc(value = "str.matches(var1,regex)", description = "字符串正则匹配。任意参数为 null 将抛出异常。", example = "str.matches('123','\\\\d+')")
    public static boolean matches(Object str, Object regex) {
        Assert.notNull(str, "str.matches -> str 不能为空");
        Assert.notNull(regex, "str.matches -> regex 不能为空");
        return str.toString().matches(regex.toString());
    }

    @FunctionDoc(value = "str.contains(var1,sub)", description = "字符串包含判断。任意参数为 null 将抛出异常。", example = "str.contains('hello','ell')")
    public static boolean contains(Object str, Object subStr) {
        Assert.notNull(str, "str.contains -> str 不能为空");
        Assert.notNull(subStr, "str.contains -> subStr 不能为空");
        return str.toString().contains(subStr.toString());
    }

    @FunctionDoc(value = "str.startsWith(var1,prefix)", description = "判断是否以前缀开始。任意参数为 null 将抛出异常。", example = "str.startsWith('hello','he')")
    public static boolean startsWith(Object str, Object prefix) {
        Assert.notNull(str, "str.startsWith -> str 不能为空");
        Assert.notNull(prefix, "str.startsWith -> prefix 不能为空");
        return str.toString().startsWith(prefix.toString());
    }

    @FunctionDoc(value = "str.endsWith(var1,suffix)", description = "判断是否以后缀结束。任意参数为 null 将抛出异常。", example = "str.endsWith('hello','lo')")
    public static boolean endsWith(Object str, Object suffix) {
        Assert.notNull(str, "str.endsWith -> str 不能为空");
        Assert.notNull(suffix, "str.endsWith -> suffix 不能为空");
        return str.toString().endsWith(suffix.toString());
    }

    @FunctionDoc(value = "str.trim(var1)", description = "去除前后空格。参数为 null 将抛出异常。", example = "str.trim('  hello  ')")
    public static String trim(Object str) {
        Assert.notNull(str, "str.trim -> str 不能为空");
        return str.toString().trim();
    }

    @FunctionDoc(value = "str.toLowerCase(var1)", description = "转小写。参数为 null 将抛出异常。", example = "str.toLowerCase('HELLO')")
    public static String toLowerCase(Object str) {
        Assert.notNull(str, "str.toLowerCase -> str 不能为空");
        return str.toString().toLowerCase();
    }

    @FunctionDoc(value = "str.toUpperCase(var1)", description = "转大写。参数为 null 将抛出异常。", example = "str.toUpperCase('hello')")
    public static String toUpperCase(Object str) {
        Assert.notNull(str, "str.toUpperCase -> str 不能为空");
        return str.toString().toUpperCase();
    }

    @FunctionDoc(value = "str.length(var1)", description = "获取字符串长度。参数为 null 将抛出异常。", example = "str.length('abc')")
    public static int length(Object str) {
        Assert.notNull(str, "str.length -> str 不能为空");
        return str.toString().length();
    }

    @FunctionDoc(value = "str.isEmpty(var1)", description = "判断是否为空 (null或\"\")。参数为 null 将抛出异常。", example = "str.isEmpty('')")
    public static boolean isEmpty(Object str) {
        Assert.notNull(str, "str.isEmpty -> str 不能为空");
        return str.toString().isEmpty();
    }

    @FunctionDoc(value = "str.isBlank(var1)", description = "判断是否为空白 (不可见字符)。参数为 null 将抛出异常。", example = "str.isBlank('  ')")
    public static boolean isBlank(Object str) {
        Assert.notNull(str, "str.isBlank -> str 不能为空");
        return StrUtil.isBlank(str.toString());
    }

    @FunctionDoc(value = "str.isNotBlank(var1)", description = "判断是否不为空白。参数为 null 将抛出异常。", example = "str.isNotBlank('abc')")
    public static boolean isNotBlank(Object str) {
        Assert.notNull(str, "str.isNotBlank -> str 不能为空");
        return StrUtil.isNotBlank(str.toString());
    }

    @FunctionDoc(value = "str.isNotEmpty(var1)", description = "判断是否不为空。参数为 null 将抛出异常。", example = "str.isNotEmpty('abc')")
    public static boolean isNotEmpty(Object str) {
        Assert.notNull(str, "str.isNotEmpty -> str 不能为空");
        return !str.toString().isEmpty();
    }

    @FunctionDoc(value = "str.isNumeric(var1)", description = "判断是否全为数字。参数为 null 将抛出异常。", example = "str.isNumeric('123')")
    public static boolean isNumeric(Object str) {
        Assert.notNull(str, "str.isNumeric -> str 不能为空");
        return StrUtil.isNumeric(str.toString());
    }


    @FunctionDoc(value = "str.clearMarkdown(text)", description = "清除markdown格式。",
            example = "str.clearMarkdown('```json\\n{\\n    \\\"test\\\": \\\"12323\\\"\\n}\\n```')",
            response = "{\"test\": \"12323\"}"
    )
    public static String clearMarkdown(String str) {
        return MarkdownUtils.tryClearMarkdown(str);
    }
}