package com.myflow.rule;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/6/29
 */
@Data
@Builder
public class RuleAction {
    private String key;
    private Rule whenRule;
    private List<Action> thenActions;
    private List<Action> otherwiseActions;
}
