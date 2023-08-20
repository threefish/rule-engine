package cn.xjbpm.rule.engine.definition.model.activity;

import cn.xjbpm.rule.engine.definition.model.NodeType;
import cn.xjbpm.rule.engine.definition.model.activity.decisiontable.DecisionTablesRow;
import lombok.Data;

import java.util.List;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/28
 */
@Data
public class DecisionTablesNode extends ActivityNode {


    List<DecisionTablesRow> tableRows;

    @Override
    public NodeType getType() {
        return NodeType.DecisionTablesNode;
    }
}
