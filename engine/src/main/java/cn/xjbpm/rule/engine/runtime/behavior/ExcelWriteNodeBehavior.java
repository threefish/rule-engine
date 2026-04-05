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

import cn.xjbpm.rule.common.utils.ExcelUtils;
import cn.xjbpm.rule.engine.aviator.AviatorContext;
import cn.xjbpm.rule.engine.aviator.AviatorExecutor;
import cn.xjbpm.rule.engine.definition.model.nodes.ExcelWriteNode;
import cn.xjbpm.rule.engine.runtime.model.FlowContext;
import cn.xjbpm.rule.properties.RuleProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Excel写入节点行为处理器
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Slf4j
@SuppressWarnings("all")
public class ExcelWriteNodeBehavior implements NodeBehavior {

    private final ExcelWriteNode node;

    public ExcelWriteNodeBehavior(ExcelWriteNode node) {
        this.node = node;
    }

    @Override
    public void execution(FlowContext context) throws Exception {

        log.info("执行Excel写入节点: {}", node.getName());

        ExcelWriteExecutionContext execContext = resolveExpressions(context);

        List<Map<String, Object>> data = getDataFromVariable(execContext.dataVariable);

        ExcelUtils.ExcelWriteResult result = writeExcel(execContext, data);

        Map<String, Object> resultMap = buildResultMap(result, execContext);
        context.setNodeOutput(node.getId(), resultMap);

        if (context.isDebugModel()) {
            context.addTraceLog(node.getId(), "Excel写入完成: {} 行数据", new Object[]{result.getRowCount()});
        }
    }

    /**
     * 一次性解析所有表达式，避免重复执行
     */
    private ExcelWriteExecutionContext resolveExpressions(FlowContext context) {
        Map<String, Object> variable = context.getVariable();
        RuleProperties ruleProperties = context.getEngineServices().getRuleProperties();
        String ruleFlowKey = context.getProcessInstance().getRuleFlowKey();
        String filePath = evaluateString(node.getFilePath(), variable);
        Path path = Paths.get(ruleProperties.getAttachmentPath(), ruleFlowKey, node.getId(), filePath);
        return ExcelWriteExecutionContext.builder()
                .filePath(path.toString())
                .sheetName(evaluateString(node.getSheetName(), variable))
                .dataVariable(AviatorExecutor.execute(AviatorContext.create(node.getDataVariable(), context.getVariable())))
                .columnMapping(evaluateString(node.getColumnMapping(), variable))
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
    private List<Map<String, Object>> getDataFromVariable(Object dataVariable) {
        if (dataVariable instanceof List) {
            return (List<Map<String, Object>>) dataVariable;
        }
        throw new IllegalArgumentException("数据源变量类型不正确，应为List<Map<String, Object>>: " + dataVariable);
    }

    /**
     * 写入Excel
     */
    private ExcelUtils.ExcelWriteResult writeExcel(ExcelWriteExecutionContext execContext, List<Map<String, Object>> data) throws Exception {
        Assert.hasText(execContext.filePath, "文件路径不能为空");
        Assert.notEmpty(data, "数据不能为空");

        return ExcelUtils.write(
                execContext.filePath,
                execContext.sheetName,
                node.getWriteMode(),
                data,
                node.getStartRow(),
                node.getStartCol(),
                node.isHasHeader(),
                execContext.columnMapping,
                node.getDateFormat(),
                node.isAutoSizeColumn(),
                node.isCreateDirectory()
        );
    }

    /**
     * 构建结果映射
     */
    private Map<String, Object> buildResultMap(ExcelUtils.ExcelWriteResult result, ExcelWriteExecutionContext execContext) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("filePath", result.getFilePath());
        resultMap.put("sheetName", result.getSheetName());
        resultMap.put("rowCount", result.getRowCount());
        resultMap.put("writeMode", result.getWriteMode());
        return resultMap;
    }

    /**
     * Excel写入执行上下文，缓存已解析的表达式结果
     */
    @lombok.Builder
    @lombok.Data
    private static class ExcelWriteExecutionContext {
        private String filePath;
        private String sheetName;
        private Object dataVariable;
        private String columnMapping;
    }
}
