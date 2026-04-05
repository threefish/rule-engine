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

package cn.xjbpm.rule.engine.runtime.behavior;

import cn.xjbpm.rule.common.utils.StringUtils;
import cn.xjbpm.rule.custom.CredentialsManager;
import cn.xjbpm.rule.engine.aviator.AviatorContext;
import cn.xjbpm.rule.engine.aviator.AviatorExecutor;
import cn.xjbpm.rule.engine.definition.model.nodes.GitNode;
import cn.xjbpm.rule.engine.definition.model.nodes.GitNode.OperationType;
import cn.xjbpm.rule.engine.runtime.behavior.model.FileModel;
import cn.xjbpm.rule.engine.runtime.model.FlowContext;
import cn.xjbpm.rule.engine.runtime.model.credentials.GitCredential;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.internal.storage.file.WindowCache;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.storage.file.WindowCacheConfig;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.util.Assert;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Git节点行为处理器，使用JGit实现clone/pull操作
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Slf4j
public class GitNodeBehavior implements NodeBehavior {

    private final GitNode node;

    public GitNodeBehavior(GitNode node) {
        this.node = node;
    }

    @Override
    public void execution(FlowContext context) throws Exception {
        GitCredential credential = resolveCredential(context);
        GitExecutionContext execContext = resolveExpressions(context);

        log.info("执行Git节点: {}", node.getName());

        if (execContext.getOperationType() == OperationType.CLONE && Boolean.TRUE.equals(node.getCleanBeforeExecution())) {
            WindowCache.reconfigure(new WindowCacheConfig());
            cleanDirectory(execContext.localPath);
            if (context.isDebugModel()) {
                context.addTraceLog(node.getId(), "已清空目录: {}", execContext.localPath);
            }
        }

        try {
            this.executeOperation(context, credential, execContext);
        } finally {
            WindowCache.reconfigure(new WindowCacheConfig());
        }

        if (context.isDebugModel()) {
            context.addTraceLog(node.getId(), "Git操作完成: {}", execContext.operationType);
        }
    }

    /**
     * 解析凭据
     */
    private GitCredential resolveCredential(FlowContext context) {
        if (StringUtils.isNotBlank(node.getCredentialId())) {
            CredentialsManager credentialsManager = context.getEngineServices().getCredentialsManager();
            return credentialsManager.getGitCredential(node.getCredentialId());
        }
        return null;
    }

    /**
     * 解析表达式
     */
    private GitExecutionContext resolveExpressions(FlowContext context) {
        Map<String, Object> variable = context.getVariable();
        return GitExecutionContext.builder()
                .operationType(node.getOperationType())
                .repoUrl(evaluateString(node.getRepoUrl(), variable))
                .localPath(evaluateString(node.getLocalPath(), variable))
                .branch(evaluateString(node.getBranch(), variable))
                .build();
    }

    /**
     * 执行Git操作
     */
    private Map<String, Object> executeOperation(FlowContext context, GitCredential credential, GitExecutionContext ctx) {
        Map<String, Object> result = new HashMap<>();
        result.put("operationType", ctx.operationType);
        try {
            switch (ctx.operationType) {
                case CLONE -> executeClone(credential, ctx, result);
                case PULL -> executePull(credential, ctx, result);
                default -> throw new UnsupportedOperationException("Unknown OperationType: " + ctx.operationType);
            }
            result.put("success", true);
        } catch (Exception e) {
            log.error("Git节点操作失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("errorMessage", e.getMessage());
            throw new RuntimeException(e);
        } finally {
            context.setNodeOutput(node.getId(), result);
        }
        return result;
    }

    /**
     * 执行Clone操作
     */
    private void executeClone(GitCredential credential, GitExecutionContext ctx, Map<String, Object> result) throws Exception {
        File localDir = new File(ctx.localPath);
        log.info("Git clone: {} -> {} (branch: {})", ctx.repoUrl, ctx.localPath, ctx.branch);

        Assert.hasText(ctx.repoUrl, "仓库地址不能为空");

        CloneCommand cloneCommand = Git.cloneRepository()
                .setURI(ctx.repoUrl)
                .setDirectory(localDir);
        UsernamePasswordCredentialsProvider credentialsProvider = buildCredentialsProvider(credential);
        if (credentialsProvider != null) {
            cloneCommand.setCredentialsProvider(credentialsProvider);
        }
        if (StringUtils.isNotBlank(ctx.branch)) {
            cloneCommand.setBranch(ctx.branch);
        }

        try (Git git = cloneCommand.call()) {
            result.put(RESULT, "clone成功");
            result.put("localPath", ctx.localPath);
            result.put("branch", git.getRepository().getBranch());
            result.put("commitId", resolveHeadCommitId(git));
            result.put("file", FileModel.of(localDir));
        }
    }

    /**
     * 执行Pull操作
     */
    private void executePull(GitCredential credential, GitExecutionContext ctx, Map<String, Object> result) throws Exception {
        File localDir = new File(ctx.localPath);
        log.info("Git pull: {}", ctx.localPath);

        try (Git git = Git.open(localDir)) {
            var pullCommand = git.pull();
            UsernamePasswordCredentialsProvider credentialsProvider = buildCredentialsProvider(credential);
            if (credentialsProvider != null) {
                pullCommand.setCredentialsProvider(credentialsProvider);
            }
            PullResult pullResult = pullCommand.call();

            result.put(RESULT, pullResult.isSuccessful() ? "pull成功" : "pull失败");
            result.put("localPath", ctx.localPath);
            result.put("commitId", resolveHeadCommitId(git));
            if (pullResult.getMergeResult() != null) {
                result.put("mergeResult", pullResult.getMergeResult().getMergeStatus().toString());
            }
            result.put("file", FileModel.of(localDir));
        }
    }

    /**
     * 构建凭据提供器
     */
    private UsernamePasswordCredentialsProvider buildCredentialsProvider(GitCredential credential) {
        if (credential == null) {
            return null;
        }
        String username = credential.getUsername() != null ? credential.getUsername() : "";
        String password = credential.getPassword() != null ? credential.getPassword() : "";
        return new UsernamePasswordCredentialsProvider(username, password);
    }

    /**
     * 获取HEAD的commitId
     */
    private String resolveHeadCommitId(Git git) {
        try {
            Ref head = git.getRepository().findRef("HEAD");
            if (head != null) {
                ObjectId objectId = head.getObjectId();
                if (objectId != null) {
                    return objectId.getName();
                }
            }
        } catch (Exception e) {
            log.warn("无法获取HEAD commitId: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 清空指定目录及其内容
     */
    private void cleanDirectory(String path) {
        Path dirPath = Paths.get(path);
        if (!Files.exists(dirPath)) {
            return;
        }
        log.info("清空目录: {}", path);
        try {
            FileUtils.deleteDirectory(dirPath.toFile());
        } catch (Exception e) {
            log.warn("清空目录失败: {}", e.getMessage());
        }
    }

    private String evaluateString(String expression, Map<String, Object> variable) {
        if (expression == null) {
            return null;
        }
        return AviatorExecutor.evaluateString(AviatorContext.create(expression, variable));
    }

    @lombok.Builder
    @lombok.Data
    private static class GitExecutionContext {
        private OperationType operationType;
        private String repoUrl;
        private String localPath;
        private String branch;
    }
}
