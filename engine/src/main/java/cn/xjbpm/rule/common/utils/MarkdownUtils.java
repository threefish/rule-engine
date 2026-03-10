package cn.xjbpm.rule.common.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *  AI 响应处理工具
 */
public class MarkdownUtils {

    // 匹配以 ``` 开头并以 ``` 结尾的结构，忽略前后的空白字符
    // (?s) 开启单行模式，使 . 匹配包括换行符在内的所有字符
    private static final Pattern MARKDOWN_BLOCK_PATTERN = Pattern.compile("(?s)^\\s*```[a-zA-Z-]*\\s+(.*?)\\s*```\\s*$");

    /**
     * 仅当字符串被 Markdown 代码块包裹时，提取其中的纯文本内容
     * 适用于提取 AI 返回的 JSON 结构或格式化文档
     * * @param str 原始字符串
     * @return 处理后的文本；若不符合格式则返回原字符串
     */
    public static String tryClearMarkdown(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        Matcher matcher = MARKDOWN_BLOCK_PATTERN.matcher(str);
        if (matcher.find()) {
            // 返回第一个捕获组的内容，即代码块内部的正文
            return matcher.group(1).trim();
        }

        // 不符合格式，原样返回，避免破坏正常数据
        return str;
    }
}