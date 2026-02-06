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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/30
 */
public final class FilePathValidatorUtils {

    private static final Pattern WINDOWS_ILLEGAL_CHARS =
            Pattern.compile("[\\\\/:*?\"<>|]");
    private static final List<String> WINDOWS_RESERVED_NAMES = Arrays.asList(
            "CON", "PRN", "AUX", "NUL",
            "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9",
            "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"
    );


    private FilePathValidatorUtils() {
    }

    /**
     * 校验文件路径是否可用于“写入”
     *
     * @param filePath 完整文件路径（包含文件名）
     */
    public static void validateForWrite(String filePath) throws IOException {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("文件路径不能为空");
        }
        if (filePath.indexOf('\0') >= 0) {
            throw new IllegalArgumentException("文件路径包含非法字符 \\0");
        }
        Path path = Paths.get(filePath);
        // 1. 校验文件名
        validateFileName(path.getFileName().toString());
        // 文件路径不存在时，先创建文件夹
        Files.createDirectories(path.getParent());
    }

    private static void validateFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        if (fileName.length() > 255) {
            throw new IllegalArgumentException("文件名长度超过 255 字符");
        }

        for (char c : fileName.toCharArray()) {
            if (Character.isISOControl(c)) {
                throw new IllegalArgumentException("文件名包含控制字符");
            }
        }

        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            validateWindowsFileName(fileName);
        } else {
            validateUnixFileName(fileName);
        }
    }

    private static void validateWindowsFileName(String fileName) {
        if (WINDOWS_ILLEGAL_CHARS.matcher(fileName).find()) {
            throw new IllegalArgumentException("Windows 文件名包含非法字符: " + fileName);
        }

        if (fileName.endsWith(" ") || fileName.endsWith(".")) {
            throw new IllegalArgumentException("Windows 文件名不能以空格或点结尾");
        }

        String baseName = fileName.contains(".")
                          ? fileName.substring(0, fileName.indexOf('.'))
                          : fileName;

        if (WINDOWS_RESERVED_NAMES.contains(baseName.toUpperCase())) {
            throw new IllegalArgumentException("Windows 保留文件名: " + fileName);
        }
    }

    private static void validateUnixFileName(String fileName) {
        if (fileName.indexOf('/') >= 0) {
            throw new IllegalArgumentException("Unix 文件名不能包含 '/'");
        }
    }




    /* ======================= 目标文件校验 ======================= */

    public static void validateTargetFile(Path path) {
        if (Files.exists(path)) {
            if (!Files.isRegularFile(path)) {
                throw new IllegalArgumentException("目标路径不是普通文件: " + path);
            }
            if (!Files.isWritable(path)) {
                throw new IllegalArgumentException("目标文件不可写: " + path);
            }
        }
    }
}