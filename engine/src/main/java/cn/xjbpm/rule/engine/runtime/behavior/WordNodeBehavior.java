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

import cn.hutool.core.io.FileUtil;
import cn.xjbpm.rule.engine.aviator.AviatorContext;
import cn.xjbpm.rule.engine.aviator.AviatorExecutor;
import cn.xjbpm.rule.engine.definition.model.nodes.WordNode;
import cn.xjbpm.rule.engine.runtime.model.FlowContext;
import cn.xjbpm.rule.properties.RuleProperties;
import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.config.Configure;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Word文档生成节点行为处理器
 * 基于poi-tl模板引擎实现Word文档生成
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Slf4j
public class WordNodeBehavior implements NodeBehavior {

    private final WordNode node;

    public WordNodeBehavior(WordNode node) {
        this.node = node;
    }

    @Override
    public void execution(FlowContext context) throws Exception {
        log.info("执行Word生成节点: {}", node.getName());

        WordExecutionContext execContext = resolveExpressions(context);

        Map<String, Object> data = getDataFromVariable(context, execContext.dataVariable);

        generateWordDocument(execContext, data);

        Map<String, Object> resultMap = buildResultMap(execContext);
        context.setNodeOutput(node.getId(), resultMap);

        if (context.isDebugModel()) {
            context.addTraceLog(node.getId(), "Word文档生成完成: {}", new Object[]{execContext.outputPath});
        }
    }

    /**
     * 解析所有表达式
     */
    private WordExecutionContext resolveExpressions(FlowContext context) {
        Map<String, Object> variable = context.getVariable();
        RuleProperties ruleProperties = context.getEngineServices().getRuleProperties();
        String ruleFlowKey = context.getProcessInstance().getRuleFlowKey();

        String templatePath = evaluateString(node.getTemplatePath(), variable);
        String outputPath = evaluateString(node.getOutputPath(), variable);

        Path attachmentPath = Paths.get(ruleProperties.getAttachmentPath(), ruleFlowKey, node.getId());

        if (!new File(templatePath).isAbsolute()) {
            templatePath = Paths.get(ruleProperties.getAttachmentPath(), templatePath).toString();
        }

        if (!new File(outputPath).isAbsolute()) {
            outputPath = attachmentPath.resolve(outputPath).toString();
        }

        return WordExecutionContext.builder()
                .templatePath(templatePath)
                .outputPath(outputPath)
                .dataVariable(node.getDataVariable())
                .useSpringEL(node.isUseSpringEL())
                .createDirectory(node.isCreateDirectory())
                .build();
    }

    private String evaluateString(String expression, Map<String, Object> variable) {
        if (expression == null) {
            return null;
        }
        return AviatorExecutor.evaluateString(AviatorContext.create(expression, variable));
    }

    /**
     * 从变量中获取数据
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getDataFromVariable(FlowContext context, String dataVariable) {
        Object data = AviatorExecutor.execute(AviatorContext.create(dataVariable, context.getVariable()));
        if (data instanceof Map) {
            return (Map<String, Object>) data;
        }
        throw new IllegalArgumentException("数据源变量类型不正确，应为Map<String, Object>: " + dataVariable);
    }

    /**
     * 生成Word文档
     */
    private void generateWordDocument(WordExecutionContext execContext, Map<String, Object> data) throws Exception {
        Assert.hasText(execContext.templatePath, "模板文件路径不能为空");
        Assert.hasText(execContext.outputPath, "输出文件路径不能为空");
        Assert.notNull(data, "数据不能为空");

        File templateFile = new File(execContext.templatePath);
        Assert.isTrue(templateFile.exists(), "模板文件不存在: " + execContext.templatePath);

        if (execContext.createDirectory) {
            File outputFile = new File(execContext.outputPath);
            FileUtil.mkParentDirs(outputFile);
        }

        Configure config = Configure.builder()
                .useSpringEL(execContext.useSpringEL)
                .build();

        try (XWPFTemplate template = XWPFTemplate.compile(execContext.templatePath, config)) {
            template.render(data);
            template.writeToFile(execContext.outputPath);
        }

        log.info("Word文档生成成功: {}", execContext.outputPath);
    }

    /**
     * 构建结果映射
     */
    private Map<String, Object> buildResultMap(WordExecutionContext execContext) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("templatePath", execContext.templatePath);
        resultMap.put("outputPath", execContext.outputPath);
        resultMap.put("dataVariable", execContext.dataVariable);
        return resultMap;
    }

    /**
     * Word执行上下文
     */
    @lombok.Builder
    @lombok.Data
    private static class WordExecutionContext {
        private String templatePath;
        private String outputPath;
        private String dataVariable;
        private boolean useSpringEL;
        private boolean createDirectory;
    }
}
