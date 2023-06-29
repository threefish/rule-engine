package com.myflow.rule;

import java.util.List;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/6/29
 */
public class RuleSet {
    private String name;
    private String type;//loop,simple
    private String loopVariableName;
    private String key;
    private List<RuleAction> ruleActions;
}
