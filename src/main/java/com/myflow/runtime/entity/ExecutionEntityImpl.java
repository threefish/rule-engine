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
@Builder
@Data
@NoArgsConstructor
public class ExecutionEntityImpl implements ExecutionEntity {


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

    /**
     * 活动状态
     */
    private boolean active;
    /**
     * 是否已结束
     */
    private boolean isEnded;

    public ExecutionEntityImpl(Long processInstanceId, Map<String, Object> variable) {
        this.id = processInstanceId;
        this.processInstanceId = processInstanceId;
        this.variable = variable;
    }

    public ExecutionEntityImpl(Long id, Long processInstanceId, Map<String, Object> variable) {
        this.id = id;
        this.processInstanceId = processInstanceId;
        this.variable = variable;
    }

    @Override
    public ExecutionEntity createChild() {
        ExecutionEntity executionEntity = new ExecutionEntityImpl();
        executionEntity.setParentExecutionId(this.getId());
        executionEntity.setVariable(this.getVariable());
        executionEntity.setProcessInstanceId(this.getProcessInstanceId());
        executionEntity.setEnded(isEnded);
        executionEntity.setActive(active);
        return executionEntity;
    }

    /**
     * 设置当前为未活动状态
     */
    @Override
    public void inactivate() {
        this.setActive(false);
    }
}
