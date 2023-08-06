package cn.xjbpm.rule.engine.definition.model.activity.scoringcard;

import cn.xjbpm.rule.engine.rule.Rule;
import lombok.Data;

import java.util.List;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/8/6
 */
@Data
public class ScoringCardRow {
    String rowKey;
    double weight;
    double score;
    List<Rule> conditionColumns;
}
