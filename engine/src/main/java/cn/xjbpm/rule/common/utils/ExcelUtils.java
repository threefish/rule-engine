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

import cn.xjbpm.rule.engine.definition.model.nodes.ExcelReadNode;
import cn.xjbpm.rule.engine.definition.model.nodes.ExcelWriteNode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Excel 工具类
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Slf4j
public class ExcelUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 读取Excel文件
     */
    public static ExcelReadResult read(String filePath, String sheetName,
                                       ExcelReadNode.ReadRangeType readRangeType,
                                       int startRow, Integer endRow,
                                       int startCol, Integer endCol,
                                       boolean hasHeader, Integer maxRowCount,
                                       ExcelReadNode.EmptyCellHandling emptyCellHandling) throws Exception {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("Excel文件不存在: " + filePath);
        }

        try (
                FileInputStream fis = new FileInputStream(file);
                Workbook workbook = createWorkbook(fis, filePath)
        ) {

            Sheet sheet = getSheet(workbook, sheetName);
            if (sheet == null) {
                throw new IOException("工作表不存在: " + sheetName);
            }

            int firstRowNum = sheet.getFirstRowNum();
            int lastRowNum = sheet.getLastRowNum();

            int actualStartRow = readRangeType == ExcelReadNode.ReadRangeType.RANGE
                                 ? Math.max(startRow - 1, firstRowNum)
                                 : firstRowNum;
            int actualEndRow = readRangeType == ExcelReadNode.ReadRangeType.RANGE && endRow != null
                               ? Math.min(endRow - 1, lastRowNum)
                               : lastRowNum;

            List<String> headers = new ArrayList<>();
            List<Map<String, Object>> data = new ArrayList<>();

            int headerRowIndex = actualStartRow;
            int dataStartRowIndex = hasHeader ? actualStartRow + 1 : actualStartRow;

            if (hasHeader && actualStartRow <= lastRowNum) {
                Row headerRow = sheet.getRow(headerRowIndex);
                if (headerRow != null) {
                    int colStart = startCol - 1;
                    int colEnd = endCol != null ? endCol - 1 : headerRow.getLastCellNum() - 1;
                    for (int i = colStart; i <= colEnd; i++) {
                        Cell cell = headerRow.getCell(i);
                        headers.add(getCellValueAsString(cell));
                    }
                }
            }

            int rowCount = 0;
            for (int rowIndex = dataStartRowIndex; rowIndex <= actualEndRow; rowIndex++) {
                if (maxRowCount != null && rowCount >= maxRowCount) {
                    break;
                }

                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }

                int colStart = startCol - 1;
                int colEnd = endCol != null ? endCol - 1 : row.getLastCellNum() - 1;

                Map<String, Object> rowData = new LinkedHashMap<>();
                boolean skipRow = false;
                boolean hasEmptyCell = false;

                for (int colIndex = colStart; colIndex <= colEnd; colIndex++) {
                    Cell cell = row.getCell(colIndex);
                    Object value = getCellValue(cell);

                    if (value == null || (value instanceof String && StringUtils.isBlank((String) value))) {
                        hasEmptyCell = true;
                        if (emptyCellHandling == ExcelReadNode.EmptyCellHandling.SKIP_ROW) {
                            skipRow = true;
                            break;
                        } else if (emptyCellHandling == ExcelReadNode.EmptyCellHandling.EMPTY_STRING) {
                            value = "";
                        }
                    }

                    String key = hasHeader && (colIndex - colStart) < headers.size()
                                 ? headers.get(colIndex - colStart)
                                 : "col" + (colIndex - colStart + 1);
                    rowData.put(key, value);
                }

                if (skipRow) {
                    continue;
                }

                data.add(rowData);
                rowCount++;
            }

            log.info("Excel读取成功: {} 行数据, {} 列", data.size(), headers.size());

            return ExcelReadResult.builder()
                    .headers(headers)
                    .data(data)
                    .rowCount(data.size())
                    .sheetName(sheet.getSheetName())
                    .build();
        }
    }

    /**
     * 写入Excel文件
     *
     * @param filePath        文件路径
     * @param sheetName       工作表名称
     * @param writeMode       写入模式
     * @param data            数据列表
     * @param startRow        起始行（从1开始）
     * @param startCol        起始列（从1开始）
     * @param hasHeader       是否写入表头
     * @param columnMapping   列映射（JSON格式）
     * @param dateFormat      日期格式
     * @param autoSizeColumn  自动调整列宽
     * @param createDirectory 自动创建目录
     */
    public static ExcelWriteResult write( String filePath, String sheetName,
                                         ExcelWriteNode.WriteMode writeMode,
                                         List<Map<String, Object>> data,
                                         int startRow, int startCol,
                                         boolean hasHeader, String columnMapping,
                                         String dateFormat, boolean autoSizeColumn,
                                         boolean createDirectory) throws Exception {
        File file = new File(filePath);

        if (createDirectory && !file.getParentFile().exists()) {
            if (!file.getParentFile().mkdirs()) {
                throw new IOException("无法创建目录: " + file.getParentFile().getAbsolutePath());
            }
        }

        Map<String, String> columnMappingMap = parseColumnMapping(columnMapping);
        List<String> orderedColumns = getOrderedColumns(data, columnMappingMap);

        Workbook workbook;
        Sheet sheet;
        boolean fileExists = file.exists();

        if (fileExists) {
            try (FileInputStream fis = new FileInputStream(file)) {
                workbook = createWorkbook(fis, filePath);
            }
        } else {
            workbook = createNewWorkbook(filePath);
        }

        sheet = getOrCreateSheet(workbook, sheetName, writeMode, fileExists);

        int actualStartRow = startRow - 1;
        if (writeMode == ExcelWriteNode.WriteMode.APPEND) {
            actualStartRow = sheet.getLastRowNum() + 1;
        } else if (writeMode == ExcelWriteNode.WriteMode.OVERWRITE) {
            clearSheet(sheet);
        }

        int currentRow = actualStartRow;
        int colStart = startCol - 1;

        if (hasHeader && writeMode != ExcelWriteNode.WriteMode.APPEND) {
            Row headerRow = sheet.createRow(currentRow++);
            for (int i = 0; i < orderedColumns.size(); i++) {
                String columnName = orderedColumns.get(i);
                String displayName = columnMappingMap.getOrDefault(columnName, columnName);
                Cell cell = headerRow.createCell(colStart + i);
                cell.setCellValue(displayName);
                setHeaderStyle(workbook, cell);
            }
        }

        SimpleDateFormat sdf = new SimpleDateFormat(
                StringUtils.isNotBlank(dateFormat) ? dateFormat : "yyyy-MM-dd HH:mm:ss"
        );

        for (Map<String, Object> rowData : data) {
            Row row = sheet.createRow(currentRow++);
            for (int i = 0; i < orderedColumns.size(); i++) {
                String columnName = orderedColumns.get(i);
                Object value = rowData.get(columnName);
                Cell cell = row.createCell(colStart + i);
                setCellValue(cell, value, sdf);
            }
        }

        if (autoSizeColumn) {
            for (int i = 0; i < orderedColumns.size(); i++) {
                sheet.autoSizeColumn(colStart + i);
            }
        }

        try (FileOutputStream fos = new FileOutputStream(file)) {
            workbook.write(fos);
        }

        workbook.close();

        log.info("Excel写入成功: {} 行数据 -> {}", data.size(), filePath);

        return ExcelWriteResult.builder()
                .filePath(filePath)
                .sheetName(sheet.getSheetName())
                .rowCount(data.size())
                .writeMode(writeMode.name())
                .build();
    }

    /**
     * 创建新Workbook
     */
    private static Workbook createNewWorkbook(String filePath) {
        String lowerCasePath = filePath.toLowerCase();
        if (lowerCasePath.endsWith(".xlsx")) {
            return new XSSFWorkbook();
        } else {
            return new HSSFWorkbook();
        }
    }

    /**
     * 根据文件扩展名创建Workbook
     */
    private static Workbook createWorkbook(FileInputStream fis, String filePath) throws IOException {
        String lowerCasePath = filePath.toLowerCase();
        if (lowerCasePath.endsWith(".xlsx")) {
            return new XSSFWorkbook(fis);
        } else if (lowerCasePath.endsWith(".xls")) {
            return new HSSFWorkbook(fis);
        } else {
            throw new IOException("不支持的Excel文件格式，仅支持.xlsx和.xls");
        }
    }

    /**
     * 获取或创建工作表
     */
    private static Sheet getOrCreateSheet(Workbook workbook, String sheetName,
                                          ExcelWriteNode.WriteMode writeMode, boolean fileExists) {
        if (StringUtils.isBlank(sheetName)) {
            if (workbook.getNumberOfSheets() > 0) {
                return workbook.getSheetAt(0);
            }
            return workbook.createSheet();
        }

        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null || writeMode == ExcelWriteNode.WriteMode.NEW_SHEET) {
            String newSheetName = sheetName;
            int counter = 1;
            while (workbook.getSheet(newSheetName) != null) {
                newSheetName = sheetName + "_" + counter++;
            }
            return workbook.createSheet(newSheetName);
        }
        return sheet;
    }

    /**
     * 清空工作表
     */
    private static void clearSheet(Sheet sheet) {
        int lastRow = sheet.getLastRowNum();
        for (int i = lastRow; i >= 0; i--) {
            Row row = sheet.getRow(i);
            if (row != null) {
                sheet.removeRow(row);
            }
        }
    }

    /**
     * 解析列映射
     */
    private static Map<String, String> parseColumnMapping(String columnMapping) {
        if (StringUtils.isBlank(columnMapping)) {
            return new LinkedHashMap<>();
        }
        try {
            return OBJECT_MAPPER.readValue(columnMapping, new TypeReference<LinkedHashMap<String, String>>() {
            });
        } catch (Exception e) {
            log.warn("解析列映射失败: {}", e.getMessage());
            return new LinkedHashMap<>();
        }
    }

    /**
     * 获取有序列名列表
     */
    private static List<String> getOrderedColumns(List<Map<String, Object>> data, Map<String, String> columnMapping) {
        if (!columnMapping.isEmpty()) {
            return new ArrayList<>(columnMapping.keySet());
        }
        if (!data.isEmpty()) {
            return new ArrayList<>(data.get(0).keySet());
        }
        return new ArrayList<>();
    }

    /**
     * 设置表头样式
     */
    private static void setHeaderStyle(Workbook workbook, Cell cell) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        cell.setCellStyle(style);
    }

    /**
     * 设置单元格值
     */
    private static void setCellValue(Cell cell, Object value, SimpleDateFormat sdf) {
        if (value == null) {
            cell.setBlank();
            return;
        }

        if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value instanceof Date) {
            cell.setCellValue(sdf.format((Date) value));
        } else {
            cell.setCellValue(value.toString());
        }
    }

    /**
     * 获取工作表
     */
    private static Sheet getSheet(Workbook workbook, String sheetName) {
        if (StringUtils.isNotBlank(sheetName)) {
            return workbook.getSheet(sheetName);
        }
        return workbook.getSheetAt(0);
    }

    /**
     * 获取单元格值
     */
    private static Object getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        CellType cellType = cell.getCellType();
        switch (cellType) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    Date date = cell.getDateCellValue();
                    return DATETIME_FORMAT.format(date);
                }
                double numValue = cell.getNumericCellValue();
                if (numValue == (long) numValue) {
                    return (long) numValue;
                }
                return numValue;
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case FORMULA:
                return getFormulaCellValue(cell);
            case BLANK:
                return null;
            default:
                return null;
        }
    }

    /**
     * 获取公式单元格的值
     */
    private static Object getFormulaCellValue(Cell cell) {
        try {
            CellType cachedType = cell.getCachedFormulaResultType();
            switch (cachedType) {
                case STRING:
                    return cell.getStringCellValue();
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        return cell.getDateCellValue();
                    }
                    return cell.getNumericCellValue();
                case BOOLEAN:
                    return cell.getBooleanCellValue();
                default:
                    return null;
            }
        } catch (Exception e) {
            log.warn("获取公式值失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取单元格字符串值
     */
    private static String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        Object value = getCellValue(cell);
        return value != null ? value.toString() : "";
    }

    /**
     * Excel读取结果
     */
    @lombok.Builder
    @lombok.Data
    public static class ExcelReadResult {
        private List<String> headers;
        private List<Map<String, Object>> data;
        private int rowCount;
        private String sheetName;
    }

    /**
     * Excel写入结果
     */
    @lombok.Builder
    @lombok.Data
    public static class ExcelWriteResult {
        private String filePath;
        private String sheetName;
        private int rowCount;
        private String writeMode;
    }
}
