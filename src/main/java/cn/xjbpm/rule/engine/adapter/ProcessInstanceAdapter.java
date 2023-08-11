package cn.xjbpm.rule.engine.adapter;

import cn.xjbpm.rule.engine.adapter.persistence.ProcessInstanceRepository;
import cn.xjbpm.rule.engine.adapter.persistence.po.ProcessInstanceEntity;
import cn.xjbpm.rule.engine.common.enums.ProcessStatusEnum;
import cn.xjbpm.rule.engine.runtime.entity.ProcessInstance;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/8/11
 */
@Component
@AllArgsConstructor
public class ProcessInstanceAdapter {

    private final ProcessInstanceRepository processInstanceRepository;

    public ProcessInstance createProcessInstance(long id, Long processDefinitionId, String processDefinitionKey) {
        ProcessInstanceEntity processInstanceEntity = ProcessInstanceEntity.builder()
                .id(id)
                .processDefinitionKey(processDefinitionKey)
                .processDefinitionId(processDefinitionId)
                .processStatus(ProcessStatusEnum.UNDERWAY)
                .build();
        processInstanceRepository.save(processInstanceEntity);
        ProcessInstance processInstance = new ProcessInstance();
        BeanUtils.copyProperties(processInstanceEntity, processInstance);
        return processInstance;
    }
}
