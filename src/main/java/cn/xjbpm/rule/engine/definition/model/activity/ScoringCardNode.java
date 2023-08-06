package cn.xjbpm.rule.engine.definition.model.activity;

import cn.xjbpm.rule.engine.definition.model.NodeType;
import cn.xjbpm.rule.engine.definition.model.activity.scoringcard.ScoringCalcMethodEnums;
import cn.xjbpm.rule.engine.definition.model.activity.scoringcard.ScoringCardRow;
import cn.xjbpm.rule.engine.definition.model.activity.scoringcard.ScoringCardWeight;
import lombok.Data;

import java.util.List;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/28
 */
@Data
public class ScoringCardNode extends ActivityNode {
    /**
     * 条件列
     */
    List<String> conditionColumns;
    /**
     * 统计方式
     */
    ScoringCalcMethodEnums scoringCalcMethod;
    /**
     * 赋值字段
     */
    String assignmentFiled;
    /**
     * 权重跨行组合
     */
    List<ScoringCardWeight> weights;

    /**
     * 列表数据
     */
    List<ScoringCardRow> dataList;

    String rowKey;

    @Override
    public NodeType getType() {
        return NodeType.ScoringCardNode;
    }
}
