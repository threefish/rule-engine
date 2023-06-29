package com.myflow.rule;

import java.util.List;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/6/29
 */
public class RuleAction {
    private String key;
    private Rule whenRule;
    private List<Action> thenActions;
    private List<Action> otherwiseActions;
 }
