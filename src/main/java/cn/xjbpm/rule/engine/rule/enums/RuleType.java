package cn.xjbpm.rule.engine.rule.enums;

import lombok.AllArgsConstructor;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/6/30
 */
@AllArgsConstructor
public enum RuleType {

    rule("规则"),

    group("规则组"),

    ;

    String descript;
}
