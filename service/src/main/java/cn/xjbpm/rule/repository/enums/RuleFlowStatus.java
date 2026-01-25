package cn.xjbpm.rule.repository.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2025/12/2
 */
@AllArgsConstructor
@Getter
public enum RuleFlowStatus {

    DEPLOYED("已部署"),

    UNDEPLOYED("未部署"),

    DISABLED("已禁用");

    String name;
}
