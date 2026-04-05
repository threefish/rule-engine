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

package cn.xjbpm.rule.engine.definition.model.nodes;

import cn.xjbpm.rule.engine.definition.model.enums.NodeType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

/**
 * Maven节点，用于执行Maven构建命令。
 * <p>
 * 基于Apache Maven Invoker实现，支持程序化调用Maven构建。
 * <p>
 * 输出变量（写入FlowContext）：
 * <ul>
 *   <li>{@code success}     — 是否成功</li>
 *   <li>{@code exitCode}    — Maven退出码（0表示成功）</li>
 *   <li>{@code goals}       — 执行的Maven目标</li>
 *   <li>{@code errorMessage}— 错误信息（失败时）</li>
 *   <li>{@code outputFile}  — 构建日志文件（如果配置）</li>
 * </ul>
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MavenNode extends Node {

    /**
     * 项目根目录路径（pom.xml所在目录），支持Aviator表达式
     */
    private String baseDirectory;

    /**
     * Maven目标列表，如 ["clean", "package"]，支持Aviator表达式
     */
    private List<String> goals;

    /**
     * Maven Profile列表，支持Aviator表达式
     */
    private List<String> profiles;

    /**
     * Maven属性，如 {"skipTests": "true"}，支持Aviator表达式
     */
    private Map<String, String> otherProperties;

    /**
     * 是否跳过测试
     */
    private Boolean skipTests;

    /**
     * 是否离线模式
     */
    private Boolean offline;

    /**
     * 是否调试模式（输出详细日志）
     */
    private Boolean debug;

    /**
     * 本地仓库路径，可选
     */
    private String localRepositoryPath;

    /**
     * Maven Home路径，可选（不填自动检测）
     */
    private String mavenHome;

    /**
     * POM文件路径，可选（默认使用baseDirectory/pom.xml）
     */
    private String pomFile;

    /**
     * 构建输出文件路径，可选（用于捕获构建日志）
     */
    private String outputFile;



    /**
     * 线程数（并行构建）
     */
    private Integer threads;

    /**
     * 是否也构建依赖模块
     */
    private Boolean alsoMake;

    /**
     * 指定构建的模块
     */
    private List<String> projects;

    /**
     * 额外的Maven选项，如 "-Xmx512m"
     */
    private String mavenOpts;

    @Override
    public NodeType getType() {
        return NodeType.MavenNode;
    }
}
