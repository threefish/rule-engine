package cn.xjbpm.rule.engine.runtime.behavior;

import cn.hutool.core.collection.CollUtil;
import cn.xjbpm.rule.engine.definition.model.Node;
import cn.xjbpm.rule.engine.definition.model.activity.ScoringCardNode;
import cn.xjbpm.rule.engine.definition.model.activity.scoringcard.ScoringCardRow;
import cn.xjbpm.rule.engine.rule.Rule;
import cn.xjbpm.rule.engine.runtime.entity.ExecutionEntity;
import cn.xjbpm.rule.engine.runtime.util.ConditionUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

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
    public void doExecution(ExecutionEntity executionEntity) {
        log.info("开始执行评分卡");
        List<ScoringCardRow> dataList = node.getDataList();
        List<ScoreRowValue> values = new ArrayList<>();
        if (CollUtil.isNotEmpty(dataList)) {
            for (int i = 0; i < dataList.size(); i++) {
                ScoringCardRow scoringCardRow = dataList.get(i);
                List<Rule> conditionColumns = scoringCardRow.getConditionColumns();
                boolean status = conditionColumns.stream().allMatch(rule -> ConditionUtil.resolve(rule.getExpressionCacheString(), executionEntity.getVariable()));
                if (status) {
                    values.add(new ScoreRowValue(i + 1, scoringCardRow.getScore(), scoringCardRow.getWeight()));
                }
            }
        }
        System.out.println(values);
    }

    @Override
    public Node getCurrentNode() {
        return node;
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class ScoreRowValue {
        int rowIndex;
        double value;
        double weight;
    }

}
