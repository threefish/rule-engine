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

import cn.hutool.core.collection.CollUtil;
import cn.xjbpm.rule.engine.aviator.AviatorContext;
import cn.xjbpm.rule.engine.aviator.AviatorExecutor;
import cn.xjbpm.rule.engine.definition.model.nodes.MavenNode;
import cn.xjbpm.rule.engine.runtime.behavior.model.FileModel;
import cn.xjbpm.rule.engine.runtime.model.FlowContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.shared.invoker.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.*;

/**
 * Maven节点行为处理器，使用Maven Invoker实现Maven构建
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Slf4j
public class MavenNodeBehavior implements NodeBehavior {

    private final MavenNode node;

    public MavenNodeBehavior(MavenNode node) {
        this.node = node;
    }

    @Override
    public void execution(FlowContext context) throws Exception {
        MavenExecutionContext execContext = resolveExpressions(context);

        log.info("执行Maven节点: {}", node.getName());

        this.executeOperation(context, execContext);

        if (context.isDebugModel()) {
            context.addTraceLog(node.getId(), "Maven操作完成: {}", execContext.goals);
        }
    }

    /**
     * 解析表达式
     */
    private MavenExecutionContext resolveExpressions(FlowContext context) {
        Map<String, Object> variable = context.getVariable();
        return MavenExecutionContext.builder()
                .baseDirectory(evaluateString(node.getBaseDirectory(), variable))
                .goals(evaluateList(node.getGoals(), variable))
                .profiles(evaluateList(node.getProfiles(), variable))
                .otherProperties(evaluateMap(node.getOtherProperties(), variable))
                .skipTests(node.getSkipTests())
                .offline(node.getOffline())
                .debug(node.getDebug())
                .localRepositoryPath(evaluateString(node.getLocalRepositoryPath(), variable))
                .mavenHome(evaluateString(node.getMavenHome(), variable))
                .pomFile(evaluateString(node.getPomFile(), variable))
                .outputFile(evaluateString(node.getOutputFile(), variable))
                .threads(node.getThreads())
                .alsoMake(node.getAlsoMake())
                .projects(evaluateList(node.getProjects(), variable))
                .mavenOpts(evaluateString(node.getMavenOpts(), variable))
                .build();
    }

    /**
     * 执行Maven操作
     */
    private Map<String, Object> executeOperation(FlowContext context, MavenExecutionContext ctx) {
        Map<String, Object> result = new HashMap<>();
        result.put("goals", ctx.goals);
        try {
            InvocationRequest request = buildInvocationRequest(ctx);
            Invoker invoker = buildInvoker(ctx);

            InvocationResult invocationResult = invoker.execute(request);

            result.put("exitCode", invocationResult.getExitCode());
            result.put("success", invocationResult.getExitCode() == 0);

            if (invocationResult.getExecutionException() != null) {
                result.put("errorMessage", invocationResult.getExecutionException().getMessage());
            }

            if (ctx.outputFile != null) {
                result.put("outputFile", FileModel.of(new File(ctx.outputFile)));
            }

        } catch (Exception e) {
            log.error("Maven节点操作失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("errorMessage", e.getMessage());
            throw new RuntimeException(e);
        } finally {
            context.setNodeOutput(node.getId(), result);
        }
        return result;
    }

    /**
     * 构建调用请求
     */
    private InvocationRequest buildInvocationRequest(MavenExecutionContext ctx) {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setBaseDirectory(new File(ctx.baseDirectory));
        request.setGoals(ctx.goals);
        if (ctx.profiles != null && !ctx.profiles.isEmpty()) {
            request.setProfiles(ctx.profiles);
        }
        if (Boolean.TRUE.equals(ctx.skipTests)) {
            String existingOpts = ctx.mavenOpts != null ? ctx.mavenOpts : "";
            request.setMavenOpts(existingOpts + " -DskipTests=true");
        } else if (ctx.mavenOpts != null) {
            request.setMavenOpts(ctx.mavenOpts);
        }
        if (ctx.otherProperties != null && CollUtil.isNotEmpty(ctx.otherProperties)) {
            Properties properties = new Properties();
            properties.putAll(ctx.otherProperties);
            request.setProperties(properties);
        }
        if (ctx.offline != null) {
            request.setOffline(ctx.offline);
        }
        if (ctx.debug != null) {
            request.setDebug(ctx.debug);
        }
        if (ctx.pomFile != null) {
            request.setPomFile(new File(ctx.pomFile));
        }
        if (ctx.threads != null) {
            request.setThreads(String.valueOf(ctx.threads));
        }
        if (ctx.alsoMake != null) {
            request.setAlsoMake(ctx.alsoMake);
        }
        if (ctx.projects != null && !ctx.projects.isEmpty()) {
            request.setProjects(ctx.projects);
        }

        return request;
    }

    /**
     * 构建调用器
     */
    private Invoker buildInvoker(MavenExecutionContext ctx) throws Exception {
        Invoker invoker = new DefaultInvoker();

        if (ctx.mavenHome != null) {
            invoker.setMavenHome(new File(ctx.mavenHome));
        }
        if (ctx.localRepositoryPath != null) {
            invoker.setLocalRepositoryDirectory(new File(ctx.localRepositoryPath));
        }
        if (ctx.outputFile != null) {
            invoker.setOutputHandler(new PrintStreamHandler(
                    new PrintStream(new FileOutputStream(ctx.outputFile)), true));
        }

        return invoker;
    }

    private String evaluateString(String expression, Map<String, Object> variable) {
        if (expression == null) {
            return null;
        }
        return AviatorExecutor.evaluateString(AviatorContext.create(expression, variable));
    }

    @SuppressWarnings("unchecked")
    private List<String> evaluateList(List<String> expressions, Map<String, Object> variable) {
        if (expressions == null) {
            return null;
        }
        List<String> result = new ArrayList<>();
        for (String expr : expressions) {
            Object value = AviatorExecutor.evaluateString(AviatorContext.create(expr, variable));
            if (value instanceof List) {
                result.addAll((List<String>) value);
            } else if (value != null) {
                result.add(String.valueOf(value));
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> evaluateMap(Map<String, String> expressions, Map<String, Object> variable) {
        if (expressions == null) {
            return null;
        }
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, String> entry : expressions.entrySet()) {
            Object value = AviatorExecutor.evaluateString(AviatorContext.create(entry.getValue(), variable));
            if (value instanceof Map) {
                result.putAll((Map<String, String>) value);
            } else if (value != null) {
                result.put(entry.getKey(), String.valueOf(value));
            }
        }
        return result;
    }

    @lombok.Builder
    @lombok.Data
    private static class MavenExecutionContext {
        private String baseDirectory;
        private List<String> goals;
        private List<String> profiles;
        private Map<String, String> otherProperties;
        private Boolean skipTests;
        private Boolean offline;
        private Boolean debug;
        private String localRepositoryPath;
        private String mavenHome;
        private String pomFile;
        private String outputFile;
        private Integer threads;
        private Boolean alsoMake;
        private List<String> projects;
        private String mavenOpts;
    }
}
