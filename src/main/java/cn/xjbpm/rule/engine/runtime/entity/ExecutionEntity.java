package cn.xjbpm.rule.engine.runtime.entity;

import cn.xjbpm.rule.engine.definition.model.NodeType;

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
    Long getParentId();

    void setParentId(Long parentId);

    /**
     * 获取根节点流程实例ID
     */
    Long getProcessInstanceId();

    void setProcessInstanceId(Long processInstanceId);


    String getDefinitionKey();

    void setDefinitionKey(String definitionKey);

    String getDefinitionName();

    void setDefinitionName(String definitionName);

    NodeType getNodeType();

    void setNodeType(NodeType nodeType);


    /**
     * 设置当前为 未活动状态
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
    boolean isCompleted();

    /**
     * 设置结束状态
     *
     * @param isEnded
     */
    void setCompleted(boolean completed);


}
