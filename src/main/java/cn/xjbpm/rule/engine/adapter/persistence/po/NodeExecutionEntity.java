package cn.xjbpm.rule.engine.adapter.persistence.po;

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
 * date: 2022/9/30
 */
@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
@Table("node_execution")
public class NodeExecutionEntity implements ExecutionEntity {

    /**
     * 当前ID
     */
    @Id(auto = false)
    @AutoID
    @ColDefine(width = 9, type = ColType.INT)
    private Long id;
    /**
     * 上级执行ID
     */
    @Column
    @ColDefine(width = 9, type = ColType.INT)
    private Long parentId;
    /**
     * 流程实例id
     */
    @Column
    private Long processInstanceId;


    @Column
    private String definitionKey;

    @Column
    private String definitionName;

    @Column
    private NodeType nodeType;

    /**
     * 活动状态
     */
    @Column
    private boolean active;
    /**
     * 是否已完成
     */
    @Column
    private boolean completed;

    @Column
    @CreatedDate
    private LocalDateTime createTime;

    @Column
    @LastModifiedDate
    private LocalDateTime updateTime;

    /**
     * 设置当前为未活动状态
     */
    @Override
    public void inactivate() {
        this.setActive(false);
    }

}
