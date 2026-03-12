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
import cn.xjbpm.rule.engine.definition.model.nodes.ExcelReadNode;
import cn.xjbpm.rule.engine.runtime.model.FlowContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * Excel读取节点行为处理器
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Slf4j
@SuppressWarnings("all")
public class ExcelReadNodeBehavior implements NodeBehavior {

    private final ExcelReadNode node;

    public ExcelReadNodeBehavior(ExcelReadNode node) {
        this.node = node;
    }

    @Override
    public void execution(FlowContext context) throws Exception {

        log.info("执行Excel读取节点: {}", node.getName());

        ExcelReadExecutionContext execContext = resolveExpressions(context);

        ExcelUtils.ExcelReadResult result = readExcel(execContext);

        Map<String, Object> resultMap = buildResultMap(result, execContext);
        context.put(node.getId(), resultMap);

        if (context.isDebugModel()) {
            context.addTraceLog(node.getId(), "Excel读取完成: {} 行数据", new Object[]{result.getRowCount()});
        }
    }

    /**
     * 一次性解析所有表达式，避免重复执行
     */
    private ExcelReadExecutionContext resolveExpressions(FlowContext context) {
        Map<String, Object> variable = context.getVariable();
        return ExcelReadExecutionContext.builder()
                .filePath(evaluateString(node.getFilePath(), variable))
                .sheetName(evaluateString(node.getSheetName(), variable))
                .build();
    }

    private String evaluateString(String expression, Map<String, Object> variable) {
        if (expression == null) {
            return null;
        }
        return AviatorExecutor.evaluateString(AviatorContext.create(expression, variable));
    }


    /**
     * 读取Excel
     */
    private ExcelUtils.ExcelReadResult readExcel(ExcelReadExecutionContext execContext) throws Exception {
        Assert.hasText(execContext.filePath, "文件路径不能为空");

        return ExcelUtils.read(
                execContext.filePath,
                execContext.sheetName,
                node.getReadRangeType(),
                node.getStartRow(),
                node.getEndRow(),
                node.getStartCol(),
                node.getEndCol(),
                node.isHasHeader(),
                node.getMaxRowCount(),
                node.getEmptyCellHandling()
        );
    }

    /**
     * 构建结果映射
     */
    private Map<String, Object> buildResultMap(ExcelUtils.ExcelReadResult result, ExcelReadExecutionContext execContext) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("data", result.getData());
        resultMap.put("headers", result.getHeaders());
        resultMap.put("rowCount", result.getRowCount());
        resultMap.put("sheetName", result.getSheetName());
        resultMap.put("filePath", execContext.filePath);
        return resultMap;
    }

    /**
     * Excel读取执行上下文，缓存已解析的表达式结果
     */
    @lombok.Builder
    @lombok.Data
    private static class ExcelReadExecutionContext {
        private String filePath;
        private String sheetName;
    }
}
