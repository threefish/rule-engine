package com.myflow.definition.model.activity;

import com.myflow.definition.model.NodeType;
import com.myflow.rule.RuleSet;
import lombok.Data;

import java.util.List;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/28
 */
@Data
public class RuleSetNode extends ActivityNode {

    private List<RuleSet> ruleSet;

    @Override
    public NodeType getType() {
        return NodeType.RuleSetNode;
    }
}
