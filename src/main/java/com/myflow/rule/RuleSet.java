package com.myflow.rule;

import com.myflow.rule.enums.RuleSetType;
import lombok.Data;

import java.util.List;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/6/29
 */
@Data
public class RuleSet {
    private String name;
    private RuleSetType type;
    private String loopVariableName;
    private String key;
    private List<RuleAction> ruleActions;
}
