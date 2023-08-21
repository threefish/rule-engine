package cn.xjbpm.rule.engine.adapter;

import cn.xjbpm.rule.engine.adapter.persistence.ProcessVariableRepository;
import cn.xjbpm.rule.engine.adapter.persistence.po.ProcessVariableEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/8/11
 */
@Component
@AllArgsConstructor
public class ProcessVariableAdapter {

    private final ProcessVariableRepository processVariableRepository;


    public void createNodeVariable(Long nodeExecutionId, Long processIntanceId, String key, Map<String, Object> numberOfInstances) {
        ProcessVariableEntity variableEntity = ProcessVariableEntity.builder()
                .nodeExecutionId(nodeExecutionId)
                .processInstanceId(processIntanceId)
                .nodeKey(key)
                .variable(numberOfInstances)
                .build();
        processVariableRepository.save(variableEntity);
    }

    public Map<String, Object> findScopeNodeVariable(Long nodeExecutionId, String nodeKey) {
        ProcessVariableEntity one = processVariableRepository.lambdaQuery()
                .eq(ProcessVariableEntity::getNodeExecutionId, nodeExecutionId)
                .eq(ProcessVariableEntity::getNodeKey, nodeKey).one();
        return one.getVariable();
    }

    public boolean updateByProcessInstanceId(Long processIntanceId, Map<String, Object> variable) {
        return processVariableRepository.lambdaUpdate()
                .set(ProcessVariableEntity::getVariable, variable)
                .eq(ProcessVariableEntity::getProcessInstanceId, processIntanceId)
                .update() > 0;
    }
}
