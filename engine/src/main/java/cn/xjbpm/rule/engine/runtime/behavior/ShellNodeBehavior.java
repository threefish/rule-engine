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
import cn.xjbpm.rule.common.utils.StringUtils;
import cn.xjbpm.rule.engine.aviator.AviatorContext;
import cn.xjbpm.rule.engine.aviator.AviatorExecutor;
import cn.xjbpm.rule.engine.definition.model.nodes.ShellNode;
import cn.xjbpm.rule.engine.runtime.model.FlowContext;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.HashMap;
import java.util.Map;


/**
 * @author 黄川 huchuc@vip.qq.com
 */
@Slf4j
@SuppressWarnings("all")
public class ShellNodeBehavior implements NodeBehavior {

    private final ShellNode node;

    public ShellNodeBehavior(ShellNode node) {
        this.node = node;
    }


    @Override
    public void execution(FlowContext context) {
        if (RuleFlowConstant.DEMO_MODE) {
            context.addTraceLog(StringUtils.format("[{}] 演示模式不允执行,已跳过", node.getId()));
            Map<String, Object> data = new HashMap<>();
            data.put("data", "演示模式不允执行");
            data.put("errorCode", 0);
            context.put(node.getId(), data);
            return;
        }
        try {
            // 1. 解析命令字符串
            String commandStr = AviatorExecutor.evaluateString(
                    AviatorContext.create(node.getCommandStr(), context.getVariable())
            );
            // 2. 根据系统构造 ProcessBuilder
            // 使用 sh -c 或 cmd /c 可以执行复杂的管道命令
            ProcessBuilder pb;
            boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
            if (isWindows) {
                pb = new ProcessBuilder("cmd.exe", "/c", commandStr);
            } else {
                pb = new ProcessBuilder("sh", "-c", commandStr);
            }
            if (node.isCleanEnv()) {
                pb.environment().clear();
            }
            pb.redirectErrorStream(node.isRedirectError());
            if (node.isCleanEnv()) {
                pb.environment().clear();
            }
            if (StringUtils.isNotBlank(node.getDirectory())) {
                String directory = AviatorExecutor.evaluateString(
                        AviatorContext.create(node.getDirectory(), context.getVariable())
                );
                pb.directory(new File(directory));
            }
            // 3. 启动进程
            Process process = pb.start();
            // 4. 结果处理
            if (node.isWait()) {
                // 获取当前系统的编码，防止中文乱码（Windows通常是GBK，Linux是UTF-8）
                String charset = isWindows ? "GBK" : "UTF-8";
                String output = readProcessOutput(process.getInputStream(), charset);
                int errorCode = process.waitFor();
                Map<String, Object> data = new HashMap<>();
                data.put("data", output);
                data.put("errorCode", Integer.toString(errorCode));
                context.put(node.getId(), data);
                if (log.isInfoEnabled()) {
                    log.info("命令执行结束，退出码: {}", errorCode);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Shell 执行异常: " + e.getMessage(), e);
        }
    }

    private String readProcessOutput(InputStream is, String charset) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, charset))) {
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append(System.lineSeparator());
            }
            return result.toString();
        }
    }
}