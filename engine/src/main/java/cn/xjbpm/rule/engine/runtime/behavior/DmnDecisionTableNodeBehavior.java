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

import cn.xjbpm.rule.common.utils.StringUtils;
import cn.xjbpm.rule.common.utils.VariableUtils;
import cn.xjbpm.rule.engine.aviator.AviatorContext;
import cn.xjbpm.rule.engine.aviator.AviatorExecutor;
import cn.xjbpm.rule.engine.definition.model.nodes.DmnDecisionTableNode;
import cn.xjbpm.rule.engine.definition.model.nodes.decisiontable.DmnDecisionTablePolicy;
import cn.xjbpm.rule.engine.runtime.model.FlowContext;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BinaryOperator;

/**
 * DMN 1.3 规范的决策表执行引擎
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Slf4j
public class DmnDecisionTableNodeBehavior implements NodeBehavior {

    private final DmnDecisionTableNode node;

    public DmnDecisionTableNodeBehavior(DmnDecisionTableNode node) {
        this.node = node;
    }

    @Override
    public void execution(FlowContext context) {
        log.info("开始执行决策表: {}", node.getId());
        Map<String, Object> variable = context.getVariable();
        DmnDecisionTablePolicy tablePolicy = node.getDmnPolicy();
        Map<String, Object> result = this.execute(tablePolicy, variable, context);
        context.put(this.node.getId(), result);
        for (DmnDecisionTablePolicy.Column outputColumn : tablePolicy.getOutputColumns()) {
            String assignmentField = outputColumn.getAssignmentField();
            String field = outputColumn.getField();
            if (StringUtils.isNotBlank(assignmentField)) {
                VariableUtils.setPathVariableByValue(assignmentField,  result.get(field), variable);
            }
        }
    }

    public Map<String, Object> execute(DmnDecisionTablePolicy policy, Map<String, Object> input, FlowContext context) {
        List<DmnDecisionTablePolicy.Row> matched = matchRules(policy, input, context);
        if (matched.isEmpty()) {
            return Map.of();
        }
        return applyHitPolicy(policy, matched);
    }

    private List<DmnDecisionTablePolicy.Row> matchRules(DmnDecisionTablePolicy policy, Map<String, Object> input, FlowContext context) {
        List<DmnDecisionTablePolicy.Row> result = new ArrayList<>();
        AtomicInteger rowIndex = new AtomicInteger(1);
        // 保持规则定义的原始顺序 (Rule Order)
        for (DmnDecisionTablePolicy.Row row : policy.getRules()) {
            if (matchRow(row, input, context)) {
                context.addTraceLog(node.getId(), "命中规则[{}]: {}", rowIndex.getAndIncrement(), row.getDescription());
                result.add(row);
                // 优化：如果是 FIRST，且不需要收集所有命中，可以提前返回
                // 但为了代码结构的统一性，这里先收集所有，在 applyHitPolicy 处理
                if (policy.getHitPolicy() == DmnDecisionTablePolicy.HitPolicy.FIRST) {
                    return result;
                }
            } else {
                context.addTraceLog(node.getId(), "未命中规则[{}]: {}", rowIndex.getAndIncrement(), row.getDescription());
            }
        }
        return result;
    }

    private boolean matchRow(DmnDecisionTablePolicy.Row row, Map<String, Object> input, FlowContext context) {
        for (Map.Entry<String, String> entry : row.getCacheExpression().entrySet()) {
            String key = entry.getKey();
            String aviatorExpr = entry.getValue();
            if (StringUtils.isBlank(aviatorExpr)) {
                continue;
            }
            Object actualValue = resolveValue(input, key);
            if (evaluate(aviatorExpr, actualValue, context) == false) {
                return false;
            }
        }
        return true;
    }

    private boolean evaluate(String aviatorExpr, Object value, FlowContext context) {
        Object result = null;
        try {
            result = AviatorExecutor.execute(AviatorContext.create(aviatorExpr, Collections.singletonMap("x", value)));
        } finally {
            if (context.isDebugModel()) {
                context.addTraceLog(node.getId(), "表达式: {} 执行结果：{}", aviatorExpr, result);
            }
        }
        return Boolean.TRUE.equals(result);
    }


    private Map<String, Object> applyHitPolicy(DmnDecisionTablePolicy policy, List<DmnDecisionTablePolicy.Row> rows) {
        switch (policy.getHitPolicy()) {
            case UNIQUE:
                if (rows.size() != 1) {
                    throw new IllegalStateException(String.format("UNIQUE policy violated. Expected 1 match, found %d", rows.size()));
                }
                return outputOf(rows.get(0), policy);

            case FIRST:
                return outputOf(rows.get(0), policy);

            case ANY:
                // DMN: 多行命中时，所有输出值必须完全相同
                return handleAnyPolicy(policy, rows);

            case PRIORITY:
                // DMN: 基于输出列 allowedValues 排序，取优先级最高的
                return handlePriorityPolicy(policy, rows);

            case OUTPUT_ORDER:
                // DMN: 返回所有结果，按输出列 allowedValues 排序
                return handleOutputOrderPolicy(policy, rows);

            case RULE_ORDER, COLLECT:
                // DMN: 按规则定义的顺序返回列表 (rows 已经是顺序的)
                // DMN: 任意顺序列表，通常使用 Rule Order
                return collectList(policy, rows);

            case COLLECT_SUM:
                return aggregate(policy, rows, BigDecimal::add);

            case COLLECT_MIN:
                return aggregate(policy, rows, (a, b) -> a.compareTo(b) <= 0 ? a : b);

            case COLLECT_MAX:
                return aggregate(policy, rows, (a, b) -> a.compareTo(b) >= 0 ? a : b);

            case COLLECT_COUNT:
                // Count 为命中行数
                Map<String, Object> countRes = new HashMap<>();
                for (DmnDecisionTablePolicy.Column col : policy.getOutputColumns()) {
                    countRes.put(col.getField(), rows.size());
                }
                return countRes;

            default:
                throw new UnsupportedOperationException("Unknown Hit Policy: " + policy.getHitPolicy());
        }
    }

    /**
     * 实现 ANY 策略：一致性检查
     */
    private Map<String, Object> handleAnyPolicy(DmnDecisionTablePolicy policy, List<DmnDecisionTablePolicy.Row> rows) {
        if (rows.isEmpty()) {
            return Map.of();
        }

        Map<String, Object> baseline = outputOf(rows.get(0), policy);

        for (int i = 1; i < rows.size(); i++) {
            Map<String, Object> current = outputOf(rows.get(i), policy);
            // 严格的 Deep Equals
            if (!baseline.equals(current)) {
                throw new IllegalStateException("ANY policy violated. Matched rules have conflicting outputs: " + baseline + " vs " + current);
            }
        }
        return baseline;
    }

    /**
     * 实现 PRIORITY 策略
     */
    private Map<String, Object> handlePriorityPolicy(DmnDecisionTablePolicy policy, List<DmnDecisionTablePolicy.Row> rows) {
        List<DmnDecisionTablePolicy.Row> sorted = sortRowsByOutputPriority(policy, rows);
        return outputOf(sorted.get(0), policy);
    }

    /**
     * 实现 OUTPUT_ORDER 策略
     */
    private Map<String, Object> handleOutputOrderPolicy(DmnDecisionTablePolicy policy, List<DmnDecisionTablePolicy.Row> rows) {
        List<DmnDecisionTablePolicy.Row> sorted = sortRowsByOutputPriority(policy, rows);
        return collectList(policy, sorted);
    }

    /**
     * 核心排序：根据 Column 定义的 Allowed Values 计算优先级
     */
    private List<DmnDecisionTablePolicy.Row> sortRowsByOutputPriority(DmnDecisionTablePolicy policy, List<DmnDecisionTablePolicy.Row> rows) {
        List<DmnDecisionTablePolicy.Row> sorted = new ArrayList<>(rows);

        sorted.sort((r1, r2) -> {
            for (DmnDecisionTablePolicy.Column col : policy.getOutputColumns()) {
                Object v1 = parseLiteral(r1.getDynamicValue(col.getId()));
                Object v2 = parseLiteral(r2.getDynamicValue(col.getId()));

                int p1 = getPriorityIndex(col, v1);
                int p2 = getPriorityIndex(col, v2);

                if (p1 != p2) {
                    return Integer.compare(p1, p2);
                }
            }
            return 0;
        });
        return sorted;
    }

    private int getPriorityIndex(DmnDecisionTablePolicy.Column col, Object value) {
        // 如果没有定义 allowedValues，无法确定优先级
        if (col.getAllowedValues() == null || col.getAllowedValues().isEmpty()) {
            return Integer.MAX_VALUE;
        }

        String strVal = String.valueOf(value);
        int idx = col.getAllowedValues().indexOf(strVal);

        // 如果该值不在允许列表中，优先级设为最低（或抛出异常）
        return idx == -1 ? Integer.MAX_VALUE : idx;
    }

    // --- 辅助方法 ---

    private Map<String, Object> outputOf(DmnDecisionTablePolicy.Row row, DmnDecisionTablePolicy policy) {
        Map<String, Object> out = new HashMap<>();
        for (DmnDecisionTablePolicy.Column col : policy.getOutputColumns()) {
            out.put(col.getField(), parseLiteral(row.getDynamicValue(col.getId())));
        }
        return out;
    }

    private Map<String, Object> collectList(DmnDecisionTablePolicy policy, List<DmnDecisionTablePolicy.Row> rows) {
        Map<String, List<Object>> result = new HashMap<>();
        for (DmnDecisionTablePolicy.Row row : rows) {
            for (DmnDecisionTablePolicy.Column col : policy.getOutputColumns()) {
                result.computeIfAbsent(col.getField(), k -> new ArrayList<>())
                        .add(parseLiteral(row.getDynamicValue(col.getId())));
            }
        }
        return new HashMap<>(result);
    }

    private Map<String, Object> aggregate(DmnDecisionTablePolicy policy, List<DmnDecisionTablePolicy.Row> rows, BinaryOperator<BigDecimal> op) {
        Map<String, BigDecimal> result = new HashMap<>();
        for (DmnDecisionTablePolicy.Row row : rows) {
            for (DmnDecisionTablePolicy.Column col : policy.getOutputColumns()) {
                Object raw = parseLiteral(row.getDynamicValue(col.getId()));
                if (raw == null) {
                    continue;
                }
                try {
                    BigDecimal val = new BigDecimal(raw.toString());
                    result.merge(col.getField(), val, op);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Cannot aggregate non-numeric value: " + raw);
                }
            }
        }
        return new HashMap<>(result);
    }

    /**
     * 解析字面量：去引号，去下划线，转数字
     */
    private Object parseLiteral(String val) {
        if (val == null) {
            return null;
        }
        val = val.trim();

        // String
        if (val.startsWith("\"") && val.endsWith("\"")) {
            return val.substring(1, val.length() - 1);
        }

        // Boolean
        if ("true".equalsIgnoreCase(val)) {
            return true;
        }
        if ("false".equalsIgnoreCase(val)) {
            return false;
        }

        // Numeric (Handle underscores e.g. 1_000_000)
        // 匹配可选负号 + 数字 + 可选下划线 + 可选小数
        if (val.matches("-?[\\d_]+(\\.[\\d_]+)?")) {
            String cleanNum = val.replace("_", "");
            // 优先返回 BigDecimal 以保证精度，特别是对于 COLLECT_SUM
            if (cleanNum.contains(".")) {
                return new BigDecimal(cleanNum);
            }
            // 对于整数，为了后续比较方便，也可以统一转 BigDecimal，或者 Integer/Long
            // 这里统一转 BigDecimal 最安全
            return new BigDecimal(cleanNum);
        }

        return val;
    }

    private Object resolveValue(Map<String, Object> input, String field) {
        return VariableUtils.getByPathVariable(field, input);
    }
}