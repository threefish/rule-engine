package com.myflow.definition.model.activity;

import com.myflow.definition.model.NodeType;
import lombok.Data;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/28
 */
@Data
public class DecisionTablesNode extends ActivityNode {


    @Override
    public NodeType getType() {
        return NodeType.DecisionTablesNode;
    }
}
