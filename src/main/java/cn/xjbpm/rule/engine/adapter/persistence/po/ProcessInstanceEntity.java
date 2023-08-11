package cn.xjbpm.rule.engine.adapter.persistence.po;

import cn.xjbpm.rule.engine.common.enums.ProcessStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.enhance.annotation.AutoID;
import org.nutz.dao.entity.annotation.*;

import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/8/11
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table("process_instance")
public class ProcessInstanceEntity {

    @Id(auto = false)
    @AutoID
    @ColDefine(width = 9, type = ColType.INT)
    private  Long id;

    @Column
    private ProcessStatusEnum processStatus;

    @Column
    private Long processDefinitionId;

    @Column
    private String processDefinitionKey;

    Map<String, Object> response;


}
