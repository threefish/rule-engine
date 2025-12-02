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

    DEOPLOYED("已发布"),

    UNDEPLOYED("未发布"),

    PAUSED("暂停");

    String name;
}
