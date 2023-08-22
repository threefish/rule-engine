package cn.xjbpm.rule.engine.adapter;

import cn.xjbpm.rule.engine.adapter.persistence.NodeExecutionRepository;
import cn.xjbpm.rule.engine.adapter.persistence.po.NodeExecutionEntity;
import cn.xjbpm.rule.engine.runtime.entity.ExecutionEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/8/11
 */
@Component
@AllArgsConstructor
public class NodeExecutionAdapter {

    private final NodeExecutionRepository nodeExecutionRepository;

    public NodeExecutionEntity createExecutionEntity(NodeExecutionEntity executionEntity) {
        return nodeExecutionRepository.save(executionEntity);
    }

    public void updateExecution2Completed(ExecutionEntity executionEntity) {
        executionEntity.setCompleted(true);
        if (executionEntity instanceof NodeExecutionEntity) {
            nodeExecutionRepository.update((NodeExecutionEntity) executionEntity);
        }
    }

    public NodeExecutionEntity findNotCompletedExecution(Long processInstanceId, String key) {
        return nodeExecutionRepository.lambdaQuery()
                .eq(NodeExecutionEntity::getProcessInstanceId, processInstanceId)
                .eq(NodeExecutionEntity::getDefinitionKey, key)
                .eq(NodeExecutionEntity::isCompleted, false).one();
    }



    public NodeExecutionEntity findById(Long parentNodeExecutionId) {
        return nodeExecutionRepository.fetch(parentNodeExecutionId);
    }
}
