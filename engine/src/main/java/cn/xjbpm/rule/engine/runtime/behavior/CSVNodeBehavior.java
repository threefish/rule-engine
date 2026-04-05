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

import cn.xjbpm.rule.common.utils.CsvUtils;
import cn.xjbpm.rule.engine.aviator.AviatorContext;
import cn.xjbpm.rule.engine.aviator.AviatorExecutor;
import cn.xjbpm.rule.engine.definition.model.nodes.CSVNode;
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
 * CSV节点行为处理器
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Slf4j
@SuppressWarnings("all")
public class CSVNodeBehavior implements NodeBehavior {

    private final CSVNode node;

    public CSVNodeBehavior(CSVNode node) {
        this.node = node;
    }

    @Override
    public void execution(FlowContext context) throws Exception {

        log.info("执行CSV节点: {}", node.getName());

        Map<String, Object> resultMap;
        if (node.getOperationType() == CSVNode.OperationType.READ) {
            resultMap = readCsv(resolveReadExpressions(context));
        } else {
            resultMap = writeCsv(context, resolveWriteExpressions(context));
        }

        context.setNodeOutput(node.getId(), resultMap);

        if (context.isDebugModel()) {
            context.addTraceLog(node.getId(), "CSV {}完成", new Object[]{node.getOperationType().name()});
        }
    }

    private CsvExecutionContext resolveReadExpressions(FlowContext context) {
        Map<String, Object> variable = context.getVariable();
        RuleProperties ruleProperties = context.getEngineServices().getRuleProperties();
        String ruleFlowKey = context.getProcessInstance().getRuleFlowKey();
        String filePath = evaluateString(node.getFilePath(), variable);
        Path path = Paths.get(ruleProperties.getAttachmentPath(), filePath);
        return CsvExecutionContext.builder()
                .filePath(path.toString())
                .build();
    }

    /**
     * 一次性解析所有表达式，避免重复执行
     */
    private CsvExecutionContext resolveWriteExpressions(FlowContext context) {
        Map<String, Object> variable = context.getVariable();
        RuleProperties ruleProperties = context.getEngineServices().getRuleProperties();
        String ruleFlowKey = context.getProcessInstance().getRuleFlowKey();
        String filePath = evaluateString(node.getFilePath(), variable);
        Path path = Paths.get(ruleProperties.getAttachmentPath(), ruleFlowKey, node.getId(), filePath);
        return CsvExecutionContext.builder()
                .filePath(path.toString())
                .dataVariable(AviatorExecutor.execute(AviatorContext.create(node.getDataVariable(), context.getVariable())))
                .build();
    }

    private String evaluateString(String expression, Map<String, Object> variable) {
        if (expression == null) {
            return null;
        }
        return AviatorExecutor.evaluateString(AviatorContext.create(expression, variable));
    }


    /**
     * 读取CSV
     */
    private Map<String, Object> readCsv(CsvExecutionContext execContext) throws Exception {
        Assert.hasText(execContext.filePath, "文件路径不能为空");

        CsvUtils.CsvReadResult result = CsvUtils.read(
                execContext.filePath,
                node.getDelimiter(),
                node.getEncoding(),
                node.isHasHeader(),
                node.getStartRow(),
                node.getMaxRowCount()
        );

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("data", result.getData());
        resultMap.put("headers", result.getHeaders());
        resultMap.put("rowCount", result.getRowCount());
        resultMap.put("filePath", execContext.filePath);
        return resultMap;
    }

    /**
     * 写入CSV
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> writeCsv(FlowContext context, CsvExecutionContext execContext) throws Exception {
        Object value = execContext.dataVariable;
        if (!(value instanceof List)) {
            throw new IllegalArgumentException("数据源变量类型不正确，应为List<Map<String, Object>>: " + execContext.dataVariable);
        }
        List<Map<String, Object>> data = (List<Map<String, Object>>) value;
        Assert.notEmpty(data, "数据不能为空");

        CsvUtils.CsvWriteResult result = CsvUtils.write(
                execContext.filePath,
                node.getDelimiter(),
                node.getEncoding(),
                data,
                node.isAppendMode(),
                node.isWriteHeader(),
                true
        );

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("filePath", result.getFilePath());
        resultMap.put("rowCount", result.getRowCount());
        resultMap.put("appendMode", result.isAppendMode());
        return resultMap;
    }

    /**
     * CSV执行上下文，缓存已解析的表达式结果
     */
    @lombok.Builder
    @lombok.Data
    private static class CsvExecutionContext {
        private String filePath;
        private Object dataVariable;
    }
}
