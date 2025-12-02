package cn.xjbpm.rule.vo;

import cn.xjbpm.rule.repository.enums.RuleFlowStatus;
import lombok.Data;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2025/11/27
 */
@Data
public class RuleFlowPageQuery {


    private String key;
    private String name;
    private RuleFlowStatus status;
}
