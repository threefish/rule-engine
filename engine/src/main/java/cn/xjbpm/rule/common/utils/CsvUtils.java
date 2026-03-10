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

import cn.xjbpm.rule.engine.definition.model.nodes.CSVNode;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * CSV 工具类
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Slf4j
public class CsvUtils {

    /**
     * 读取CSV文件
     *
     * @param filePath     文件路径
     * @param delimiter    分隔符
     * @param encoding     文件编码
     * @param hasHeader    首行为表头
     * @param startRow     起始行（从1开始）
     * @param maxRowCount  最大读取行数
     * @return 读取结果
     */
    public static CsvReadResult read(String filePath, String delimiter, String encoding,
                                     boolean hasHeader, int startRow, Integer maxRowCount) throws Exception {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("CSV文件不存在: " + filePath);
        }

        Charset charset = getCharset(encoding);
        String actualDelimiter = StringUtils.isNotBlank(delimiter) ? delimiter : ",";

        List<String> headers = new ArrayList<>();
        List<Map<String, Object>> data = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset))) {
            String line;
            int currentRow = 0;
            int dataRowCount = 0;

            while ((line = reader.readLine()) != null) {
                currentRow++;

                if (currentRow < startRow) {
                    continue;
                }

                List<String> values = parseLine(line, actualDelimiter);

                if (hasHeader && currentRow == startRow) {
                    headers.addAll(values);
                    continue;
                }

                if (maxRowCount != null && dataRowCount >= maxRowCount) {
                    break;
                }

                Map<String, Object> rowData = new LinkedHashMap<>();
                for (int i = 0; i < values.size(); i++) {
                    String key = hasHeader && i < headers.size() ? headers.get(i) : "col" + (i + 1);
                    rowData.put(key, values.get(i));
                }
                data.add(rowData);
                dataRowCount++;
            }
        }

        log.info("CSV读取成功: {} 行数据, {} 列", data.size(), headers.size());

        return CsvReadResult.builder()
                .headers(headers)
                .data(data)
                .rowCount(data.size())
                .build();
    }

    /**
     * 写入CSV文件
     *
     * @param filePath      文件路径
     * @param delimiter     分隔符
     * @param encoding      文件编码
     * @param data          数据列表
     * @param appendMode    追加模式
     * @param writeHeader   写入表头
     * @param createDirectory 自动创建目录
     * @return 写入结果
     */
    public static CsvWriteResult write(String filePath, String delimiter, String encoding,
                                       List<Map<String, Object>> data, boolean appendMode,
                                       boolean writeHeader, boolean createDirectory) throws Exception {
        File file = new File(filePath);

        if (createDirectory && !file.getParentFile().exists()) {
            if (!file.getParentFile().mkdirs()) {
                throw new IOException("无法创建目录: " + file.getParentFile().getAbsolutePath());
            }
        }

        Charset charset = getCharset(encoding);
        String actualDelimiter = StringUtils.isNotBlank(delimiter) ? delimiter : ",";

        List<String> orderedColumns = new ArrayList<>();
        if (!data.isEmpty()) {
            orderedColumns.addAll(data.get(0).keySet());
        }

        boolean fileExists = file.exists() && file.length() > 0;
        boolean shouldWriteHeader = writeHeader && (!appendMode || !fileExists);

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file, appendMode && fileExists), charset))) {

            if (shouldWriteHeader && !orderedColumns.isEmpty()) {
                writer.write(String.join(actualDelimiter, orderedColumns));
                writer.newLine();
            }

            for (Map<String, Object> rowData : data) {
                List<String> values = new ArrayList<>();
                for (String column : orderedColumns) {
                    Object value = rowData.get(column);
                    values.add(escapeValue(value, actualDelimiter));
                }
                writer.write(String.join(actualDelimiter, values));
                writer.newLine();
            }
        }

        log.info("CSV写入成功: {} 行数据 -> {}", data.size(), filePath);

        return CsvWriteResult.builder()
                .filePath(filePath)
                .rowCount(data.size())
                .appendMode(appendMode)
                .build();
    }

    /**
     * 获取字符编码
     */
    private static Charset getCharset(String encoding) {
        if (StringUtils.isBlank(encoding)) {
            return StandardCharsets.UTF_8;
        }
        try {
            return Charset.forName(encoding);
        } catch (Exception e) {
            log.warn("不支持的编码格式: {}, 使用UTF-8", encoding);
            return StandardCharsets.UTF_8;
        }
    }

    /**
     * 解析CSV行
     */
    private static List<String> parseLine(String line, String delimiter) {
        List<String> result = new ArrayList<>();

        if (line == null || line.isEmpty()) {
            return result;
        }

        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (!inQuotes && line.substring(i).startsWith(delimiter)) {
                result.add(current.toString().trim());
                current = new StringBuilder();
                i += delimiter.length() - 1;
            } else {
                current.append(c);
            }
        }
        result.add(current.toString().trim());

        return result;
    }

    /**
     * 转义CSV值
     */
    private static String escapeValue(Object value, String delimiter) {
        if (value == null) {
            return "";
        }

        String strValue = value.toString();

        boolean needsQuotes = strValue.contains(delimiter) ||
                strValue.contains("\"") ||
                strValue.contains("\n") ||
                strValue.contains("\r");

        if (needsQuotes) {
            strValue = strValue.replace("\"", "\"\"");
            return "\"" + strValue + "\"";
        }

        return strValue;
    }

    /**
     * CSV读取结果
     */
    @lombok.Builder
    @lombok.Data
    public static class CsvReadResult {
        private List<String> headers;
        private List<Map<String, Object>> data;
        private int rowCount;
    }

    /**
     * CSV写入结果
     */
    @lombok.Builder
    @lombok.Data
    public static class CsvWriteResult {
        private String filePath;
        private int rowCount;
        private boolean appendMode;
    }
}
