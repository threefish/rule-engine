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
import cn.xjbpm.rule.engine.definition.model.nodes.PdfNode;
import cn.xjbpm.rule.engine.runtime.model.FlowContext;
import cn.xjbpm.rule.properties.RuleProperties;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.svgsupport.BatikSVGDrawer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * PDF文档生成节点行为处理器
 * 基于OpenHtmlToPdf组件实现HTML转PDF
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Slf4j
public class PdfNodeBehavior implements NodeBehavior {

    private final PdfNode node;

    public PdfNodeBehavior(PdfNode node) {
        this.node = node;
    }

    @Override
    public void execution(FlowContext context) throws Exception {
        log.info("执行PDF生成节点: {}", node.getName());

        PdfExecutionContext execContext = resolveExpressions(context);

        String htmlContent = getHtmlContent(execContext, context);

        generatePdfDocument(execContext, htmlContent);

        Map<String, Object> resultMap = buildResultMap(execContext);
        context.setNodeOutput(node.getId(), resultMap);

        if (context.isDebugModel()) {
            context.addTraceLog(node.getId(), "PDF文档生成完成: {}", new Object[]{execContext.outputPath});
        }
    }

    /**
     * 解析所有表达式
     */
    private PdfExecutionContext resolveExpressions(FlowContext context) {
        Map<String, Object> variable = context.getVariable();
        RuleProperties ruleProperties = context.getEngineServices().getRuleProperties();
        String ruleFlowKey = context.getProcessInstance().getRuleFlowKey();

        String htmlContent = evaluateString(node.getHtmlContent(), variable);
        String templatePath = evaluateString(node.getTemplatePath(), variable);
        String outputPath = evaluateString(node.getOutputPath(), variable);
        String baseUrl = evaluateString(node.getBaseUrl(), variable);

        Path attachmentPath = Paths.get(ruleProperties.getAttachmentPath(), ruleFlowKey, node.getId());

        if (templatePath != null && !new File(templatePath).isAbsolute()) {
            templatePath = Paths.get(ruleProperties.getAttachmentPath(), templatePath).toString();
        }

        if (outputPath != null && !new File(outputPath).isAbsolute()) {
            outputPath = attachmentPath.resolve(outputPath).toString();
        }

        return PdfExecutionContext.builder()
                .htmlContent(htmlContent)
                .templatePath(templatePath)
                .outputPath(outputPath)
                .dataVariable(node.getDataVariable())
                .baseUrl(baseUrl)
                .enableSvg(node.isEnableSvg())
                .enableFastMode(node.isEnableFastMode())
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
     * 获取HTML内容
     */
    private String getHtmlContent(PdfExecutionContext execContext, FlowContext context) throws Exception {
        if (execContext.htmlContent != null) {
            return renderTemplate(execContext.htmlContent, context);
        }

        if (execContext.templatePath != null) {
            File templateFile = new File(execContext.templatePath);
            Assert.isTrue(templateFile.exists(), "模板文件不存在: " + execContext.templatePath);
            String templateContent = FileUtil.readUtf8String(templateFile);
            return renderTemplate(templateContent, context);
        }

        throw new IllegalArgumentException("HTML内容和模板文件路径必须提供其中之一");
    }

    /**
     * 渲染模板，替换变量占位符
     */
    @SuppressWarnings("unchecked")
    private String renderTemplate(String template, FlowContext context) {
        if (node.getDataVariable() == null) {
            return template;
        }

        Object data = AviatorExecutor.execute(AviatorContext.create(node.getDataVariable(), context.getVariable()));
        if (data instanceof Map) {
            Map<String, Object> dataMap = (Map<String, Object>) data;
            String result = template;
            for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
                String placeholder = "{{" + entry.getKey() + "}}";
                String value = entry.getValue() != null ? String.valueOf(entry.getValue()) : "";
                result = result.replace(placeholder, value);
            }
            return result;
        }

        return template;
    }

    /**
     * 生成PDF文档
     */
    private void generatePdfDocument(PdfExecutionContext execContext, String htmlContent) throws Exception {
        Assert.hasText(execContext.outputPath, "输出文件路径不能为空");
        Assert.hasText(htmlContent, "HTML内容不能为空");

        if (execContext.createDirectory) {
            File outputFile = new File(execContext.outputPath);
            FileUtil.mkParentDirs(outputFile);
        }

        try (OutputStream os = new FileOutputStream(execContext.outputPath)) {
            PdfRendererBuilder builder = new PdfRendererBuilder();

            if (execContext.enableFastMode) {
                builder.useFastMode();
            }

            if (execContext.enableSvg) {
                builder.useSVGDrawer(new BatikSVGDrawer());
            }

            String baseUrl = execContext.baseUrl;
            if (baseUrl == null || baseUrl.isEmpty()) {
                baseUrl = new File(execContext.outputPath).getParent();
                if (baseUrl != null) {
                    baseUrl = "file://" + baseUrl + "/";
                }
            }

            builder.withHtmlContent(htmlContent, baseUrl);
            builder.toStream(os);
            builder.run();
        }

        log.info("PDF文档生成成功: {}", execContext.outputPath);
    }

    /**
     * 构建结果映射
     */
    private Map<String, Object> buildResultMap(PdfExecutionContext execContext) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("outputPath", execContext.outputPath);
        resultMap.put("templatePath", execContext.templatePath);
        resultMap.put("dataVariable", execContext.dataVariable);
        return resultMap;
    }

    /**
     * PDF执行上下文
     */
    @lombok.Builder
    @lombok.Data
    private static class PdfExecutionContext {
        private String htmlContent;
        private String templatePath;
        private String outputPath;
        private String dataVariable;
        private String baseUrl;
        private boolean enableSvg;
        private boolean enableFastMode;
        private boolean createDirectory;
    }
}
