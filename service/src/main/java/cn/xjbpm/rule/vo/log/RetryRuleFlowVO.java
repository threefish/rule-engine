package cn.xjbpm.rule.vo.log;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2025/12/1
 */
@Data
public class RetryRuleFlowVO {

    @NotNull
    private Long id;

    /**
     * 采用最新的规则文件
     */
    @NotNull
    private Boolean useLatest;
}
