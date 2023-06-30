package com.myflow.rule.enums;

import lombok.AllArgsConstructor;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/6/30
 */
@AllArgsConstructor
public enum RuleSetType {

    LOOP("循环"),

    SIMPLE("简单"),

    ;

    String descript;
}
