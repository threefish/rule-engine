package cn.xjbpm.rule.engine.adapter.persistence.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.enhance.annotation.AutoID;
import org.nutz.dao.enhance.annotation.CreatedDate;
import org.nutz.dao.enhance.annotation.LastModifiedDate;
import org.nutz.dao.entity.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/8/11
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table("process_variable")
public class ProcessVariableEntity {

    @Id(auto = false)
    @AutoID
    @ColDefine(width = 9, type = ColType.INT)
    private Long id;

    @Column
    private Long processInstanceId;

    /**
     * 参数
     */
    @Column
    @ColDefine(type = ColType.MYSQL_JSON)
    private Map<String, Object> variable;


    @Column
    @CreatedDate
    private LocalDateTime createTime;

    @Column
    @LastModifiedDate
    private LocalDateTime updateTime;


}
