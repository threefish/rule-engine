package cn.xjbpm.rule.engine.runtime.behavior;

import cn.hutool.core.collection.CollUtil;
import cn.xjbpm.rule.engine.common.utils.VariableUtils;
import cn.xjbpm.rule.engine.definition.model.Node;
import cn.xjbpm.rule.engine.definition.model.activity.ScoringCardNode;
import cn.xjbpm.rule.engine.definition.model.activity.scoringcard.ScoringCalcMethodEnums;
import cn.xjbpm.rule.engine.definition.model.activity.scoringcard.ScoringCardRow;
import cn.xjbpm.rule.engine.definition.model.activity.scoringcard.ScoringCardWeight;
import cn.xjbpm.rule.engine.rule.Rule;
import cn.xjbpm.rule.engine.runtime.context.ExecutionScope;
import cn.xjbpm.rule.engine.runtime.context.ProcessContextHolder;
import cn.xjbpm.rule.engine.runtime.context.ProcessRuntimeContext;
import cn.xjbpm.rule.engine.runtime.entity.ExecutionEntity;
import cn.xjbpm.rule.engine.runtime.util.ConditionUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/30
 */
@Slf4j
public class ScoringCardNodeBehavior extends BaseNodeBehavior {

    private final ScoringCardNode node;

    public ScoringCardNodeBehavior(ScoringCardNode node) {
        this.node = node;
    }

    @Override
    public void unableToComplete(ExecutionEntity executionEntity) {
        log.info("节点[{}]不满足完成条件，暂挂在此节点", node.getKey());
    }

    @Override
    public void doExecution(ExecutionEntity executionEntity, ExecutionScope executionScope) {
        log.info("开始执行评分卡");
        ProcessRuntimeContext context = ProcessContextHolder.getContext();
        Map<String, Object> variable = context.getVariable();
        List<ScoringCardRow> dataList = node.getDataList();
        List<ScoreRowValue> values = new ArrayList<>();
        if (CollUtil.isNotEmpty(dataList)) {
            for (int i = 0; i < dataList.size(); i++) {
                ScoringCardRow scoringCardRow = dataList.get(i);
                List<Rule> conditionColumns = scoringCardRow.getConditionColumns();
                boolean status = conditionColumns.stream().allMatch(rule -> ConditionUtil.resolve(rule.getExpressionCacheString(), variable));
                if (status) {
                    values.add(new ScoreRowValue(i + 1, scoringCardRow.getScore(), scoringCardRow.getWeight()));
                }
            }
        }
        String assignmentFiled = this.node.getAssignmentFiled();
        ScoringCalcMethodEnums scoringCalcMethod = this.node.getScoringCalcMethod();
        double sum;
        if (scoringCalcMethod == ScoringCalcMethodEnums.SUM_FRACTIONS) {
            sum = values.stream().mapToDouble(v -> v.getValue()).sum();
            VariableUtils.setPathVariable(assignmentFiled, "分数求和结果", sum, variable);
        } else if (scoringCalcMethod == ScoringCalcMethodEnums.SUM_WEIGHT) {
            List<ScoreRowValue> weightWalues = new ArrayList<>();
            List<ScoringCardWeight> weights = this.node.getWeights();
            for (int i = 1; i <= weights.size(); i++) {
                ScoringCardWeight weight = weights.get(i - 1);
                int start = weight.getStart();
                int end = weight.getEnd();
                double rowSum = values.stream().filter(v -> v.getRowIndex() >= start && v.getRowIndex() <= end).mapToDouble(v -> v.getValue()).sum();
                weightWalues.add(new ScoreRowValue(i, rowSum, weight.getWeight()));
            }
            sum = weightWalues.stream()
                    // 计算分数与权重的乘积
                    .mapToDouble(row -> row.getValue() * row.getWeight() / 100)
                    .sum();
            VariableUtils.setPathVariable(assignmentFiled, "加权求和结果", sum, variable);
        } else {
            throw new UnsupportedOperationException(String.format("评分卡不支持[%s]", scoringCalcMethod.getLabel()));
        }
    }

    @Override
    public Node getCurrentNode() {
        return node;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class ScoreRowValue {
        /**
         * 行序号
         */
        int rowIndex;
        /**
         * 分数
         */
        double value;
        /**
         * 权重
         */
        double weight;
    }

}
