package com.myflow.rule.enums;

import lombok.AllArgsConstructor;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/6/30
 */
@AllArgsConstructor
public enum CombinatorType {

    AND("且"),

    OR("或"),

    ;

    String descript;
}
