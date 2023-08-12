package cn.xjbpm.rule.engine.adapter.persistence.po;

import cn.xjbpm.rule.engine.common.enums.ProcessStatusEnum;
import cn.xjbpm.rule.engine.definition.model.NodeType;
import cn.xjbpm.rule.engine.runtime.entity.ExecutionEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.enhance.annotation.AutoID;
import org.nutz.dao.enhance.annotation.CreatedDate;
import org.nutz.dao.enhance.annotation.LastModifiedDate;
import org.nutz.dao.entity.annotation.*;

import java.time.LocalDateTime;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/8/11
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table("process_instance")
public class ProcessInstanceEntity implements ExecutionEntity {

    @Id(auto = false)
    @AutoID
    @ColDefine(width = 9, type = ColType.INT)
    private Long id;

    @Column
    @ColDefine(width = 9, type = ColType.INT)
    private Long parentId;

    @Column
    private ProcessStatusEnum processStatus;

    @Column
    private Long processDefinitionId;

    @Column
    private String definitionKey;

    @Column
    private String definitionName;

    @Column
    private NodeType nodeType;

    @Column
    private boolean active;

    @Column
    @CreatedDate
    private LocalDateTime createTime;

    @Column
    @LastModifiedDate
    private LocalDateTime updateTime;


    @Override
    public Long getProcessInstanceId() {
        return id;
    }

    @Override
    public void setProcessInstanceId(Long processInstanceId) {
        this.id = processInstanceId;
    }


    @Override
    public void inactivate() {
        this.active = false;
    }

    @Override
    public boolean isCompleted() {
        return this.processStatus == ProcessStatusEnum.COMPLETED;
    }

    @Override
    public void setCompleted(boolean completed) {
        if (completed) {
            this.processStatus = ProcessStatusEnum.COMPLETED;
        }
    }


}
