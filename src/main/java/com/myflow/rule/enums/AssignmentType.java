package com.myflow.rule.enums;

import lombok.AllArgsConstructor;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/6/30
 */
@AllArgsConstructor
public enum AssignmentType {

    FIXED("固定值"),

    VAR("变量值"),

    CALC("表达式计算值"),

    ;

    String descript;
}
