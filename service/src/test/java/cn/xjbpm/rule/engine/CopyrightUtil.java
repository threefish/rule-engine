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
package cn.xjbpm.rule.engine;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;


/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2025/11/20
 */
@Slf4j
public class CopyrightUtil {

    private static final String HEADER_TEXT = """
            /**
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
             """.trim();

    public static void main(String[] args) {
        // 指定你的项目目录
        String path = CopyrightUtil.class.getResource("/").getFile()
                .replaceAll("/service/target/test-classes/", "")
                .substring(1);
        Path projectDir = Paths.get(path);

        if (!Files.exists(projectDir)) {
            System.err.println("目录不存在: " + projectDir);
            return;
        }

        processDirectory(projectDir);
        System.out.println("处理完成！");
    }

    public static void processDirectory(Path rootPath) {
        try (Stream<Path> paths = Files.walk(rootPath)) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .forEach(CopyrightUtil::addHeaderToFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void addHeaderToFile(Path filePath) {
        try {
            // 1. 读取文件的所有行
            List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
            String content = String.join("\n", lines);

            // 2. 检查头部是否已经包含这段文字（这里使用开头匹配，也可以用 contains）
            if (content.trim().startsWith("/**\n * Copyright")) {
                System.out.println("跳过 (已存在): " + filePath.getFileName());
                return;
            }
            // 3. 如果没有，将头部文字和原有内容合并
            String newContent = HEADER_TEXT + "\n" + content;
            // 4. 写回文件
            Files.write(filePath, newContent.getBytes(StandardCharsets.UTF_8));
            System.out.println("已添加头部: " + filePath.getFileName());
        } catch (IOException e) {
            System.err.println("处理文件出错: " + filePath + " -> " + e.getMessage());
        }
    }
}