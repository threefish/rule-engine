package cn.xjbpm.rule.engine.definition.model.activity.decisiontable;

import lombok.Data;

import java.util.List;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/8/20
 */
@Data
public class DecisionTablesRow {

    List<DecisionTablesRowConditionColumn> conditionColumns;

    List<DecisionTablesRowAssignColumn> assignColumns;

}
