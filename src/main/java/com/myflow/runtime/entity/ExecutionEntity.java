package com.myflow.runtime.entity;

import lombok.Data;

import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/30
 */
@Data
public class ExecutionEntity {
    /**
     * 流程实例id
     */
    Long processInstanceId;
    /**
     * 参数
     */
    Map<String, Object> variable;


    public ExecutionEntity(Long processInstanceId, Map<String, Object> variable) {
        this.processInstanceId = processInstanceId;
        this.variable = variable;
    }
}
