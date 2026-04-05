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

/**
 * Git 节点，支持 clone / pull 操作（HTTPS 认证或无认证）。
 *
 * <p>输出变量（写入 FlowContext）：
 * <ul>
 *   <li>{@code result}      — 操作结果描述</li>
 *   <li>{@code localPath}   — 本地仓库路径</li>
 *   <li>{@code branch}      — 分支名（clone 时有效）</li>
 *   <li>{@code commitId}    — 操作完成后 HEAD 的 commit SHA</li>
 *   <li>{@code mergeResult} — pull 合并状态（仅 pull 时有值）</li>
 * </ul>
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GitNode extends Node {

    /**
     * 凭据 ID，对应 {@link cn.xjbpm.rule.engine.runtime.model.credentials.GitCredential}。
     * 可选，若不填则支持无需授权的公开仓库操作。
     */
    private String credentialId;

    /** 操作类型 */
    private OperationType operationType;

    /** 仓库地址，支持 Aviator 表达式 */
    private String repoUrl;

    /** 本地目录路径，支持 Aviator 表达式 */
    private String localPath;

    /**
     * 目标分支名，支持 Aviator 表达式。
     * clone 时若不填则使用远程默认分支；pull 时忽略此字段。
     */
    private String branch;

    /**
     * 执行前是否清空本地目录。
     * 若为 true，则在执行 clone 或 pull 前删除本地目录及其内容。
     */
    private Boolean cleanBeforeExecution;

    @Override
    public NodeType getType() {
        return NodeType.GitNode;
    }

    public enum OperationType {
        CLONE, PULL
    }
}
