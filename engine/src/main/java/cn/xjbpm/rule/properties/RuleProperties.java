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
package cn.xjbpm.rule.properties;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2025/12/11
 */
@Configuration
@ConfigurationProperties(prefix = "rule")
@Data
@Slf4j
public class RuleProperties {

    /**
     * 名称
     */
    private String akkaSystemName = "xj-rule";

    /**
     * 默认执行超时时间
     */
    private long akkaDefaultExecuteTimeoutSeconds = 30;
    /**
     * 全局 Worker 路由池初始大小
     */
    private int akkaGlobalWorkerPoolInitSize = 200;
    /**
     * 全局 Worker 路由池最大大小
     */
    private int akkaGlobalWorkerPoolMaxSize = 2000;
    /**
     * 附件存储路径
     */
    private String attachmentPath = System.getProperty("user.home") + File.separator + akkaSystemName;

    @PostConstruct
    public void init() {
        try {
            Path path = Paths.get(attachmentPath);
            if (Files.notExists(path)) {
                Files.createDirectories(path);
                log.info("附件存储目录创建成功: {}", attachmentPath);
            }
        } catch (IOException e) {
            log.error("初始化附件存储目录失败,使用IO节点时可能会导致失败: {}", attachmentPath, e);
        }
    }
}