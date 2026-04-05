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
import cn.xjbpm.rule.common.utils.SSHUtils;
import cn.xjbpm.rule.custom.CredentialsManager;
import cn.xjbpm.rule.engine.aviator.AviatorContext;
import cn.xjbpm.rule.engine.aviator.AviatorExecutor;
import cn.xjbpm.rule.engine.definition.model.nodes.SshNode;
import cn.xjbpm.rule.engine.runtime.behavior.model.FileModel;
import cn.xjbpm.rule.engine.runtime.model.FlowContext;
import cn.xjbpm.rule.engine.runtime.model.credentials.SshCredential;
import cn.xjbpm.rule.properties.RuleProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * SSH节点行为处理器
 * 支持执行远程命令、上传文件、下载文件三种操作
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Slf4j
@SuppressWarnings("all")
public class SshNodeBehavior implements NodeBehavior {

    private final SshNode node;

    public SshNodeBehavior(SshNode node) {
        this.node = node;
    }

    @Override
    public void execution(FlowContext context) throws Exception {
        if (RuleFlowConstant.DEMO_MODE) {
            handleDemoMode(context);
            return;
        }

        RuleProperties ruleProperties = context.getEngineServices().getRuleProperties();
        CredentialsManager credentialsManager = context.getEngineServices().getCredentialsManager();
        SshCredential sshCredential = credentialsManager.getSshCredential(node.getCredentialId());

        log.info("执行SSH节点: {}", node.getName());

        Map<String, Object> resultMap = executeSshOperation(context, ruleProperties, sshCredential);
        context.setNodeOutput(node.getId(), resultMap);

        if (context.isDebugModel()) {
            context.addTraceLog(node.getId(), "SSH result: {}", new Object[]{resultMap.get(RESULT)});
        }
    }

    /**
     * 处理演示模式
     */
    private void handleDemoMode(FlowContext context) {
        context.addTraceLog(node.getId(), "演示模式不允许执行,已跳过");
        Map<String, Object> data = new HashMap<>();
        data.put("data", "演示模式不允许执行");
        data.put("errorCode", 0);
        context.setNodeOutput(node.getId(), data);
    }

    /**
     * 执行SSH操作
     */
    private Map<String, Object> executeSshOperation(FlowContext context, RuleProperties ruleProperties, SshCredential sshCredential) throws Exception {
        SshNode.ExcuteType excuteType = node.getExcuteType();
        String result;
        String localSaveFilePath = null;
        String remoteDirectoryFilePath = null;
        String fileName = null;

        switch (excuteType) {
            case EXECUTE_COMMAND:
                result = executeCommand(context);
                break;
            case DOWNLOAD_FILE:
                result = downloadFile(context, ruleProperties);
                localSaveFilePath = AviatorExecutor.evaluateString(AviatorContext.create(node.getLocalSaveFilePath(), context.getVariable()));
                remoteDirectoryFilePath = AviatorExecutor.evaluateString(AviatorContext.create(node.getRemoteDirectoryFilePath(), context.getVariable()));
                break;
            case UPLOAD_FILE:
                result = uploadFile(context);
                localSaveFilePath = AviatorExecutor.evaluateString(AviatorContext.create(node.getLocalSaveFilePath(), context.getVariable()));
                remoteDirectoryFilePath = AviatorExecutor.evaluateString(AviatorContext.create(node.getRemoteDirectoryFilePath(), context.getVariable()));
                fileName = AviatorExecutor.evaluateString(AviatorContext.create(node.getFileName(), context.getVariable()));
                break;
            default:
                throw new IllegalArgumentException("不支持的SSH执行类型: " + excuteType);
        }

        return buildResultMap(result, fileName, localSaveFilePath, remoteDirectoryFilePath);
    }

    /**
     * 执行远程命令
     */
    private String executeCommand(FlowContext context) throws Exception {
        String commandStr = AviatorExecutor.evaluateString(AviatorContext.create(node.getCommandStr(), context.getVariable()));
        SshCredential sshCredential = context.getEngineServices().getCredentialsManager().getSshCredential(node.getCredentialId());
        return SSHUtils.execute(sshCredential, commandStr);
    }

    /**
     * 下载远程文件
     */
    private String downloadFile(FlowContext context, RuleProperties ruleProperties) throws Exception {
        String localSaveFilePath = AviatorExecutor.evaluateString(AviatorContext.create(node.getLocalSaveFilePath(), context.getVariable()));
        String remoteDirectoryFilePath = AviatorExecutor.evaluateString(AviatorContext.create(node.getRemoteDirectoryFilePath(), context.getVariable()));

        Assert.hasText(localSaveFilePath, "本地存储文件路径不能为空");
        Assert.hasText(remoteDirectoryFilePath, "远程文件路径不能为空");

        String ruleFlowKey = context.getProcessInstance().getRuleFlowKey();
        Path filePath = Paths.get(ruleProperties.getAttachmentPath(), ruleFlowKey, node.getId(), localSaveFilePath);

        SshCredential sshCredential = context.getEngineServices().getCredentialsManager().getSshCredential(node.getCredentialId());
        return SSHUtils.download(sshCredential, remoteDirectoryFilePath, filePath.toFile().getAbsolutePath());
    }

    /**
     * 上传本地文件
     */
    private String uploadFile(FlowContext context) throws Exception {
        String localSaveFilePath = AviatorExecutor.evaluateString(AviatorContext.create(node.getLocalSaveFilePath(), context.getVariable()));
        String remoteDirectoryFilePath = AviatorExecutor.evaluateString(AviatorContext.create(node.getRemoteDirectoryFilePath(), context.getVariable()));
        String fileName = AviatorExecutor.evaluateString(AviatorContext.create(node.getFileName(), context.getVariable()));

        Assert.hasText(localSaveFilePath, "本地存储文件路径不能为空");
        Assert.hasText(remoteDirectoryFilePath, "远程文件路径不能为空");

        SshCredential sshCredential = context.getEngineServices().getCredentialsManager().getSshCredential(node.getCredentialId());
        return SSHUtils.upload(sshCredential, localSaveFilePath, remoteDirectoryFilePath, fileName);
    }

    /**
     * 构建结果映射
     */
    private Map<String, Object> buildResultMap(String result, String fileName, String localSaveFilePath, String remoteDirectoryFilePath) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put(RESULT, result);
        resultMap.put("fileName", fileName);

        if (Objects.nonNull(localSaveFilePath)) {
            resultMap.put("localFile", FileModel.of(new File(localSaveFilePath)));
        }

        if (Objects.nonNull(remoteDirectoryFilePath)) {
            resultMap.put("remotePath", FileModel.of(new File(remoteDirectoryFilePath)));
        }

        return resultMap;
    }
}