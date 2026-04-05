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
package cn.xjbpm.rule.service.storage;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import cn.xjbpm.rule.properties.RuleProperties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Slf4j
@Component
@AllArgsConstructor
public class LocalDiskStorageProvider implements StorageProvider {

    private final RuleProperties ruleProperties;
    private static final String FILE_SUFFIX = ".json.gz";
    private static final String DATE_FORMAT = "yyyyMMdd";

    @Override
    public String store(String key, String content) {
        if (StrUtil.isBlank(content)) {
            return null;
        }

        try {
            // 1. 构造存储目录和相对路径
            String dateDir = DateUtil.format(DateUtil.date(), DATE_FORMAT);
            String fileName = key + FILE_SUFFIX;

            // 获取根路径并确保目录存在
            Path rootPath = Paths.get(ruleProperties.getSnapshotStoragePath()).toAbsolutePath().normalize();
            Path targetPath = rootPath.resolve(dateDir).resolve(fileName).normalize();

            // 2. 压缩数据
            byte[] compressed = ZipUtil.gzip(content, CharsetUtil.UTF_8);

            // 3. 写入文件（FileUtil.writeBytes 内部会自动创建父目录）
            FileUtil.writeBytes(compressed, targetPath.toFile());

            // 4. 计算相对路径：返回 dateDir/fileName
            return rootPath.relativize(targetPath).toString();

        } catch (Exception e) {
            log.error("本地磁盘存储快照失败 [key: {}]: {}", key, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public String fetch(String filePath) {
        if (StrUtil.isBlank(filePath)) {
            return null;
        }

        try {
            Path rootPath = Paths.get(ruleProperties.getSnapshotStoragePath()).toAbsolutePath().normalize();
            // 使用 resolve 组合路径，并通过 normalize 防止目录穿越
            Path fullPath = rootPath.resolve(filePath).normalize();

            // 安全检查：确保读取的文件依然在存储根目录之内
            if (!fullPath.startsWith(rootPath)) {
                log.warn("检测到非法的路径访问尝试: {}", filePath);
                return null;
            }

            File file = fullPath.toFile();
            if (!FileUtil.exist(file)) {
                log.warn("文件不存在: {}", fullPath);
                return null;
            }

            // 读取并解压
            byte[] bytes = FileUtil.readBytes(file);
            return ZipUtil.unGzip(bytes, CharsetUtil.UTF_8);

        } catch (Exception e) {
            log.error("读取本地快照失败 [path: {}]: {}", filePath, e.getMessage(), e);
            return null;
        }
    }
}