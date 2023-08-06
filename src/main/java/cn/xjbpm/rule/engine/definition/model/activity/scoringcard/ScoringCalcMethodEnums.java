package cn.xjbpm.rule.engine.definition.model.activity.scoringcard;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/8/6
 */
@AllArgsConstructor
@Getter
public enum ScoringCalcMethodEnums {
    SUM_FRACTIONS("分数求和"),
    SUM_WEIGHT("加权求和"),
    CALC("表达式计算");

    String label;
}
