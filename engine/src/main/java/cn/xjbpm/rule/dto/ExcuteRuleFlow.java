package cn.xjbpm.rule.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;
import java.util.Set;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2025/12/5
 */
@Data
public class ExcuteRuleFlow {

    /**
     * 规则流定义key
     */
    @NotBlank
    private String key;

    /**
     * 请求ID
     */
    private String requestId;
    /**
     * 规则流变量
     */
    @NotNull
    private Map<String, Object> variables;
    /**
     * 规则流定义内容
     */
    private String content;
    /**
     * 重试原始ID
     */
    private Long retryOriginId;
    /**
     * 异步执行
     */
    private boolean asyncExcute;
    /**
     * 跳过节点
     */
    private Set<String> skipNodeIds;
    /**
     * 调试模式
     */
    private boolean debugModel;

}
