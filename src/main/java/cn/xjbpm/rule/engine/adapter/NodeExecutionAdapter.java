package cn.xjbpm.rule.engine.adapter;

import cn.xjbpm.rule.engine.adapter.persistence.NodeExecutionRepository;
import cn.xjbpm.rule.engine.adapter.persistence.po.NodeExecutionEntity;
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
}
