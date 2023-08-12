package cn.xjbpm.rule.engine.runtime.entity;

import cn.xjbpm.rule.engine.common.enums.ProcessStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/8/11
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProcessInstance {

    private Long id;

    private Long processDefinitionId;

    private String processDefinitionKey;

    private Map<String, Object> response;

    private ProcessStatusEnum processStatus;


}
