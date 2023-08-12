package cn.xjbpm.rule.engine.adapter;

import cn.xjbpm.rule.engine.adapter.persistence.ProcessInstanceRepository;
import cn.xjbpm.rule.engine.adapter.persistence.ProcessVariableRepository;
import cn.xjbpm.rule.engine.adapter.persistence.po.ProcessDefinitionEntity;
import cn.xjbpm.rule.engine.adapter.persistence.po.ProcessInstanceEntity;
import cn.xjbpm.rule.engine.adapter.persistence.po.ProcessVariableEntity;
import cn.xjbpm.rule.engine.common.enums.ProcessStatusEnum;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/8/11
 */
@Component
@AllArgsConstructor
public class ProcessInstanceAdapter {

    private final ProcessInstanceRepository processInstanceRepository;
    private final ProcessVariableRepository processVariableRepository;

    public ProcessInstanceEntity createProcessInstance(Long processInstanceId, ProcessDefinitionEntity processDefinition, Map<String, Object> runtimeVar) {

        ProcessInstanceEntity processInstanceEntity = ProcessInstanceEntity.builder()
                .id(processInstanceId)
                .definitionKey(processDefinition.getDefinitionKey())
                .definitionName(processDefinition.getDefinitionName())
                .processDefinitionId(processDefinition.getId())
                .processStatus(ProcessStatusEnum.UNDERWAY)
                .active(true)
                .build();
        ProcessVariableEntity processVariableEntity = ProcessVariableEntity.builder()
                .processInstanceId(processInstanceId)
                .variable(runtimeVar)
                .build();
        processInstanceRepository.save(processInstanceEntity);
        processVariableRepository.save(processVariableEntity);

        return processInstanceEntity;
    }
}
