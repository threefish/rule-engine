/*
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
package cn.xjbpm.rule.engine.runtime.behavior;

import cn.xjbpm.rule.common.constant.RuleFlowConstant;
import cn.xjbpm.rule.common.utils.SSHUtils;
import cn.xjbpm.rule.common.utils.StringUtils;
import cn.xjbpm.rule.custom.CredentialsManager;
import cn.xjbpm.rule.engine.aviator.AviatorContext;
import cn.xjbpm.rule.engine.aviator.AviatorExecutor;
import cn.xjbpm.rule.engine.definition.model.nodes.SshNode;
import cn.xjbpm.rule.engine.runtime.behavior.model.FileModel;
import cn.xjbpm.rule.engine.runtime.model.FlowContext;
import cn.xjbpm.rule.engine.runtime.model.credentials.SshCredential;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
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
            context.addTraceLog(StringUtils.format("[{}] 演示模式不允执行,已跳过", node.getId()));
            Map<String, Object> data = new HashMap<>();
            data.put("data", "演示模式不允执行");
            data.put("errorCode", 0);
            context.put(node.getId(), data);
            return;
        }
        CredentialsManager credentialsManager = context.getBeanContextManager().getCredentialsManager();
        SshCredential sshCredential = credentialsManager.getSshCredential(node.getCredentialId());
        log.info("执行ssh节点:{}", node.getName());
        Map<String, Object> resultMap = new HashMap<>();
        String result = null;
        String localSaveFilePath = null;
        String remoteDirectoryFilePath = null;
        String fileName = null;
        if (node.getExcuteType() == SshNode.ExcuteType.EXECUTE_COMMAND) {
            String commandStr = AviatorExecutor.evaluateString(AviatorContext.create(node.getCommandStr(), context.getVariable()));
            result = SSHUtils.execute(sshCredential, commandStr);
        } else if (node.getExcuteType() == SshNode.ExcuteType.DOWNLOAD_FILE) {
            localSaveFilePath = AviatorExecutor.evaluateString(AviatorContext.create(node.getLocalSaveFilePath(), context.getVariable()));
            remoteDirectoryFilePath = AviatorExecutor.evaluateString(AviatorContext.create(node.getRemoteDirectoryFilePath(), context.getVariable()));
            Assert.hasText(localSaveFilePath, "本地存储文件路径不能为空");
            Assert.hasText(remoteDirectoryFilePath, "远程文件路径不能为空");
            result = SSHUtils.download(sshCredential, remoteDirectoryFilePath, localSaveFilePath);
        } else if (node.getExcuteType() == SshNode.ExcuteType.UPLOAD_FILE) {
            localSaveFilePath = AviatorExecutor.evaluateString(AviatorContext.create(node.getLocalSaveFilePath(), context.getVariable()));
            remoteDirectoryFilePath = AviatorExecutor.evaluateString(AviatorContext.create(node.getRemoteDirectoryFilePath(), context.getVariable()));
            Assert.hasText(localSaveFilePath, "本地存储文件路径不能为空");
            Assert.hasText(remoteDirectoryFilePath, "远程文件路径不能为空");
            fileName = AviatorExecutor.evaluateString(AviatorContext.create(node.getFileName(), context.getVariable()));
            result = SSHUtils.upload(sshCredential, localSaveFilePath, remoteDirectoryFilePath, fileName);
        }
        resultMap.put("result", result);
        resultMap.put("fileName", fileName);
        resultMap.put("localFile", FileModel.of(new File(localSaveFilePath)));
        resultMap.put("remotePath", FileModel.of(new File(remoteDirectoryFilePath)));
        context.put(node.getId(), resultMap);
        if (context.isDebugModel()) {
            context.addTraceLog(StringUtils.format("[{}] ssh result:{}", node.getId(), result));
        }
    }
}