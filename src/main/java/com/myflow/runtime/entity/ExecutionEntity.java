package com.myflow.runtime.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/30
 */
@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class ExecutionEntity {
    
    private Long id;
    private Long parentExecutionId;

    /**
     * 流程实例id
     */
    private Long processInstanceId;
    /**
     * 参数
     */
    private Map<String, Object> variable;

    public ExecutionEntity(Long processInstanceId, Map<String, Object> variable) {
        this.id = processInstanceId;
        this.processInstanceId = processInstanceId;
        this.variable = variable;
    }

    public ExecutionEntity createChild() {
        ExecutionEntity executionEntity = new ExecutionEntity();
        executionEntity.setParentExecutionId(this.getId());
        executionEntity.setVariable(this.getVariable());
        executionEntity.setProcessInstanceId(this.getProcessInstanceId());
        return executionEntity;
    }

}
