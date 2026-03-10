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

import cn.xjbpm.rule.common.constant.RuleFlowConstant;
import cn.xjbpm.rule.common.utils.FtpUtils;
import cn.xjbpm.rule.engine.aviator.AviatorContext;
import cn.xjbpm.rule.engine.aviator.AviatorExecutor;
import cn.xjbpm.rule.engine.definition.model.nodes.FTPNode;
import cn.xjbpm.rule.engine.runtime.behavior.model.FileModel;
import cn.xjbpm.rule.engine.runtime.model.FlowContext;
import cn.xjbpm.rule.engine.runtime.model.credentials.FtpCredential;
import cn.xjbpm.rule.properties.RuleProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * FTP节点行为处理器
 * 支持上传、下载、列出文件、删除文件四种操作
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Slf4j
@SuppressWarnings("all")
public class FTPNodeBehavior implements NodeBehavior {

    private final FTPNode node;

    public FTPNodeBehavior(FTPNode node) {
        this.node = node;
    }

    @Override
    public void execution(FlowContext context) throws Exception {
        if (RuleFlowConstant.DEMO_MODE) {
            handleDemoMode(context);
            return;
        }

        RuleProperties ruleProperties = context.getBeanContextManager().getRuleProperties();
        FtpCredential ftpCredential = context.getBeanContextManager().getCredentialsManager().getFtpCredential(node.getCredentialId());

        log.info("执行FTP节点: {}", node.getName());

        FtpExecutionContext execContext = resolveExpressions(context);

        Map<String, Object> resultMap = executeFtpOperation(context, ruleProperties, ftpCredential, execContext);
        context.put(node.getId(), resultMap);

        if (context.isDebugModel()) {
            context.addTraceLog(node.getId(), "FTP result: {}", new Object[]{resultMap.get(RESULT)});
        }
    }

    /**
     * 一次性解析所有表达式，避免重复执行
     */
    private FtpExecutionContext resolveExpressions(FlowContext context) {
        Map<String, Object> variable = context.getVariable();
        return FtpExecutionContext.builder()
                .localPath(evaluateString(node.getLocalPath(), variable))
                .remotePath(evaluateString(node.getRemotePath(), variable))
                .build();
    }

    private String evaluateString(String expression, Map<String, Object> variable) {
        if (expression == null) {
            return null;
        }
        return AviatorExecutor.evaluateString(AviatorContext.create(expression, variable));
    }

    /**
     * 处理演示模式
     */
    private void handleDemoMode(FlowContext context) {
        context.addTraceLog(node.getId(), "演示模式不允许执行,已跳过");
        Map<String, Object> data = new HashMap<>();
        data.put("data", "演示模式不允许执行");
        data.put("errorCode", 0);
        context.put(node.getId(), data);
    }

    /**
     * 执行FTP操作
     */
    private Map<String, Object> executeFtpOperation(FlowContext context, RuleProperties ruleProperties, FtpCredential ftpCredential, FtpExecutionContext execContext) throws Exception {
        FTPNode.OperationType operationType = node.getOperationType();
        String result;

        switch (operationType) {
            case UPLOAD:
                result = uploadFile(ftpCredential, execContext);
                break;
            case DOWNLOAD:
                result = downloadFile(context, ruleProperties, ftpCredential, execContext);
                break;
            case LIST:
                result = listFiles(ftpCredential, execContext);
                break;
            case DELETE:
                result = deleteFile(ftpCredential, execContext);
                break;
            default:
                throw new IllegalArgumentException("不支持的FTP操作类型: " + operationType);
        }

        return buildResultMap(result, execContext, operationType);
    }

    /**
     * 上传文件
     */
    private String uploadFile(FtpCredential ftpCredential, FtpExecutionContext execContext) throws Exception {
        Assert.hasText(execContext.localPath, "本地文件路径不能为空");
        Assert.hasText(execContext.remotePath, "远程目标路径不能为空");

        return FtpUtils.upload(ftpCredential, execContext.localPath, execContext.remotePath, node.isPassiveMode(), node.isBinaryMode(), node.isDirectory());
    }

    /**
     * 下载文件
     */
    private String downloadFile(FlowContext context, RuleProperties ruleProperties, FtpCredential ftpCredential, FtpExecutionContext execContext) throws Exception {
        Assert.hasText(execContext.localPath, "本地存储目录不能为空");
        Assert.hasText(execContext.remotePath, "远程文件路径不能为空");

        String ruleFlowKey = context.getProcessInstance().getRuleFlowKey();
        Path filePath = Paths.get(ruleProperties.getAttachmentPath(), ruleFlowKey, node.getId(), execContext.localPath);

        return FtpUtils.download(ftpCredential, execContext.remotePath, filePath.toFile().getAbsolutePath(), node.isPassiveMode(), node.isBinaryMode(), node.isDirectory());
    }

    /**
     * 列出文件
     */
    private String listFiles(FtpCredential ftpCredential, FtpExecutionContext execContext) throws Exception {
        Assert.hasText(execContext.remotePath, "远程目录路径不能为空");

        List<String> files = FtpUtils.listFiles(ftpCredential, execContext.remotePath, node.isPassiveMode(), node.isBinaryMode());
        return String.join(",", files);
    }

    /**
     * 删除文件
     */
    private String deleteFile(FtpCredential ftpCredential, FtpExecutionContext execContext) throws Exception {
        Assert.hasText(execContext.remotePath, "远程文件路径不能为空");

        return FtpUtils.delete(ftpCredential, execContext.remotePath, node.isPassiveMode(), node.isBinaryMode());
    }

    /**
     * 构建结果映射
     */
    private Map<String, Object> buildResultMap(String result, FtpExecutionContext execContext, FTPNode.OperationType operationType) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put(RESULT, result);
        resultMap.put("operationType", operationType.name());
        resultMap.put("directory", node.isDirectory());

        if (Objects.nonNull(execContext.localPath)) {
            resultMap.put("localFile", FileModel.of(new File(execContext.localPath)));
        }

        if (Objects.nonNull(execContext.remotePath)) {
            resultMap.put("remotePath", execContext.remotePath);
        }

        return resultMap;
    }

    /**
     * FTP执行上下文，缓存已解析的表达式结果
     */
    @lombok.Builder
    @lombok.Data
    private static class FtpExecutionContext {
        private String localPath;
        private String remotePath;
    }
}
