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
import cn.xjbpm.rule.common.utils.ConditionUtil;
import cn.xjbpm.rule.common.utils.VariableUtils;
import cn.xjbpm.rule.engine.definition.model.nodes.ScoringCardNode;
import cn.xjbpm.rule.engine.definition.model.nodes.scoringcard.ScoringCalcMethodEnums;
import cn.xjbpm.rule.engine.definition.model.nodes.scoringcard.ScoringCardRow;
import cn.xjbpm.rule.engine.definition.model.nodes.scoringcard.ScoringCardWeight;
import cn.xjbpm.rule.engine.rule.Rule;
import cn.xjbpm.rule.engine.runtime.model.FlowContext;
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
public class ScoringCardNodeBehavior implements NodeBehavior {

    private final ScoringCardNode node;

    public ScoringCardNodeBehavior(ScoringCardNode node) {
        this.node = node;
    }


    @Override
    public void execution(FlowContext context) {
        log.info("开始执行评分卡");
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
            VariableUtils.setPathVariableByValue(assignmentFiled, sum, variable);
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
            VariableUtils.setPathVariableByValue(assignmentFiled, sum, variable);
        } else {
            throw new UnsupportedOperationException(String.format("评分卡不支持[%s]", scoringCalcMethod.getLabel()));
        }
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class ScoreRowValue {
        /**
         * 行序号
         */
        private int rowIndex;
        /**
         * 分数
         */
        private double value;
        /**
         * 权重
         */
        private double weight;
    }

}