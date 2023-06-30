package com.myflow.rule.enums;

import lombok.AllArgsConstructor;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/6/30
 */
@AllArgsConstructor
public enum ActionType {
    ASSIGNMENT("赋值"),
    CONTINUE("跳出本次循环"),
    BREAK("结束循环"),
    DELETE("结束循环"),


    ;

    String descript;
}
