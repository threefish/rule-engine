package cn.xjbpm.rule.engine.model;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/30
 */
@Data
@Builder
public class CreateProcessRequest {

    /**
     * 流程定义key
     */
    private String key;
    /**
     * 流程定义ID
     */
    private Long processDefinitionId;
    /**
     * 环境
     */
    private String environment;
    /**
     * 版本号，null取最新
     */
    private Integer version;
    /**
     * 流程变量
     */
    private Map<String, Object> variable;

}
