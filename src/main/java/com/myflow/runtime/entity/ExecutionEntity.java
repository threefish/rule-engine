package com.myflow.runtime.entity;

import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/30
 */
@SuppressWarnings("all")
public interface ExecutionEntity {


    /**
     * 执行ID
     */
    Long getId();

    void setId(Long id);

    /**
     * 获取上级ID
     *
     * @return
     */
    Long getParentExecutionId();

    void setParentExecutionId(Long parentExecutionId);

    /**
     * 获取根节点流程实例ID
     */
    Long getProcessInstanceId();

    void setProcessInstanceId(Long processInstanceId);

    ExecutionEntity createChild();

    /**
     * 设置当前为未活动状态
     */
    void inactivate();

    /**
     * 返回此执行当前是否处于活动状态
     */
    boolean isActive();

    /**
     * 使此执行处于活动或非活动状态
     */
    void setActive(boolean isActive);

    /**
     * 是否已结束
     *
     * @return
     */
    boolean isEnded();

    /**
     * 设置结束状态
     *
     * @param isEnded
     */
    void setEnded(boolean isEnded);

    /**
     * 设置变量
     *
     * @param variable
     */
    void setVariable(Map<String, Object> variable);
    Map<String, Object> getVariable();
}
