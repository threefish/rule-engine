package cn.xjbpm.rule.engine;

import org.apache.poi.xwpf.usermodel.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

/**
 * 软著源码文档生成工具 - 最终增强版
 * 1. 自动过滤 Apache License / Copyright 2025 threefish 等顶部申明
 * 2. 保留类定义上方的 @author, @date 等作者信息
 * 3. 核心包（Engine/Executor等）100% 写入
 * 4. 非核心包按 50% 比例抽样，降低篇幅
 * 5. 全量清洗方法内部注释和空行，保障代码密度
 *
 * @author 黄川 (Huang Chuan)
 */
public class CopyrightDocGenerator {

    // --- 配置区 ---
    // 1. 源码根目录
    private static final String REPO_PATH = "D:\\WorkSpace\\xj-rule-engine";
    // 2. 输出文档路径
    private static final String OUTPUT_PATH = "D:\\WorkSpace\\xj-rule-engine\\软著核心精选_清洗版.docx";

    // 核心包关键字：包含以下路径的文件将 100% 保留
    private static final List<String> CORE_KEYWORDS = Arrays.asList(
            "aviator", "definition", "translate", "actor","behavior"
    );

    // 非核心包抽样率：2 表示每 2 个非核心文件取 1 个
    private static final int NON_CORE_SAMPLE_RATE = 3;

    public static void main(String[] args) {
        try {
            System.out.println("开始扫描项目并进行高密度清洗...");
            generateOptimizedDoc();
            System.out.println("------------------------------------");
            System.out.println("文档生成成功！");
            System.out.println("已处理：剔除 License，保留 Author，核心置顶，非核心减半。");
            System.out.println("输出路径：" + OUTPUT_PATH);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void generateOptimizedDoc() throws IOException {
        List<String> allLines = new ArrayList<>();
        Path rootPath = Paths.get(REPO_PATH);

        // 1. 获取所有 Java 文件并排除测试目录
        List<Path> allFiles = Files.walk(rootPath)
                .filter(p -> p.toString().endsWith(".java"))
                .filter(p -> !p.toString().contains(File.separator + "test" + File.separator))
                .filter(p -> !p.toString().contains(File.separator + "target" + File.separator))
                .sorted(Comparator.comparing(Path::toString))
                .collect(Collectors.toList());

        int nonCoreCount = 0;
        for (Path file : allFiles) {
            boolean isCore = isCoreFile(file);

            // 2. 抽样逻辑
            if (isCore) {
                processFile(file, allLines);
            } else {
                nonCoreCount++;
                if (nonCoreCount % NON_CORE_SAMPLE_RATE == 0) {
                    processFile(file, allLines);
                }
            }
        }

        // 3. 写入 Word
        writeToWord(allLines);
    }

    private static void processFile(Path file, List<String> allLines) throws IOException {
        List<String> lines = Files.readAllLines(file);
        allLines.add("// >>> File: " + file.toString().substring(REPO_PATH.length()));
        allLines.addAll(cleanCodeForCopyright(lines));
        allLines.add(""); // 文件间间距
    }

    /**
     * 核心清洗逻辑
     */
    private static List<String> cleanCodeForCopyright(List<String> lines) {
        List<String> cleaned = new ArrayList<>();
        boolean firstClassFound = false;
        boolean inBlockComment = false;
        boolean isSkippingLicense = false;

        int lineIdx = 0;
        for (String line : lines) {
            lineIdx++;
            String trimmed = line.trim();

            // A. 类定义之前的处理（处理 License 和 Author）
            if (!firstClassFound) {
                // 1. 识别块注释开始
                if (trimmed.startsWith("/*")) {
                    inBlockComment = true;

                    // 探测是否为 License 块
                    if (lineIdx < 60) {
                        // 预读后面几行判断是否包含关键字
                        String context = getLookAhead(lines, lineIdx - 1, 25).toLowerCase();
                        if (context.contains("license") || context.contains("copyright") || context.contains("threefish")) {
                            isSkippingLicense = true;
                        }
                    }
                    if (!isSkippingLicense) cleaned.add(line);
                    continue;
                }

                // 2. 块注释内部
                if (inBlockComment) {
                    if (!isSkippingLicense) cleaned.add(line);
                    if (trimmed.endsWith("*/")) {
                        inBlockComment = false;
                        isSkippingLicense = false;
                    }
                    continue;
                }

                // 3. 过滤单行 License 干扰
                if (trimmed.startsWith("//") && (trimmed.toLowerCase().contains("license") || trimmed.toLowerCase().contains("copyright"))) {
                    continue;
                }

                // 4. 识别类定义开始
                if (trimmed.contains("class ") || trimmed.contains("interface ") || trimmed.contains("enum ")) {
                    firstClassFound = true;
                    cleaned.add(line);
                } else if (!trimmed.isEmpty()) {
                    cleaned.add(line); // 保留 package, import 和正常的作者注释
                }
                continue;
            }

            // B. 类定义之后的处理（严格去噪，去方法内注释）
            if (trimmed.startsWith("/*")) { inBlockComment = true; continue; }
            if (inBlockComment) {
                if (trimmed.endsWith("*/")) inBlockComment = false;
                continue;
            }
            if (trimmed.startsWith("//")) continue;

            if (!trimmed.isEmpty()) {
                cleaned.add(line);
            }
        }
        return cleaned;
    }

    /**
     * 向前预读指定行数，用于特征识别
     */
    private static String getLookAhead(List<String> lines, int start, int offset) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < Math.min(lines.size(), start + offset); i++) {
            sb.append(lines.get(i));
        }
        return sb.toString();
    }

    private static boolean isCoreFile(Path p) {
        String pathStr = p.toString().replace("\\", "/");
        return CORE_KEYWORDS.stream().anyMatch(pathStr::contains);
    }

    private static void writeToWord(List<String> content) throws IOException {
        try (XWPFDocument document = new XWPFDocument();
             FileOutputStream out = new FileOutputStream(OUTPUT_PATH)) {

            XWPFParagraph paragraph = document.createParagraph();
            paragraph.setAlignment(ParagraphAlignment.LEFT);
            paragraph.setSpacingBetween(1.0); // 紧凑行距

            XWPFRun run = paragraph.createRun();
            run.setFontSize(9); // 小五号
            run.setFontFamily("Courier New");

            for (String line : content) {
                run.setText(line);
                run.addBreak();
            }
            document.write(out);
        }
    }
}