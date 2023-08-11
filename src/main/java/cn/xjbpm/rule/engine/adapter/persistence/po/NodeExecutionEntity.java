package cn.xjbpm.rule.engine.adapter.persistence.po;

import cn.xjbpm.rule.engine.runtime.entity.ExecutionEntity;
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
public class NodeExecutionEntity implements ExecutionEntity {

    /**
     * 当前ID
     */
    private Long id;
    /**
     * 上级执行ID
     */
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
     * 是否已完成
     */
    private boolean completed;

    public NodeExecutionEntity(Long processInstanceId, Map<String, Object> variable) {
        this.id = processInstanceId;
        this.processInstanceId = processInstanceId;
        this.variable = variable;
    }

    public NodeExecutionEntity(Long id, Long processInstanceId, Map<String, Object> variable) {
        this.id = id;
        this.processInstanceId = processInstanceId;
        this.variable = variable;
    }

    @Override
    public ExecutionEntity createChild() {
        ExecutionEntity executionEntity = new NodeExecutionEntity();
        executionEntity.setParentExecutionId(this.getId());
        executionEntity.setVariable(this.getVariable());
        executionEntity.setProcessInstanceId(this.getProcessInstanceId());
        executionEntity.setCompleted(completed);
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
