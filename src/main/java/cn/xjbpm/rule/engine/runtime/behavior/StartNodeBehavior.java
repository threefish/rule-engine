package cn.xjbpm.rule.engine.runtime.behavior;

import cn.xjbpm.rule.engine.definition.model.Node;
import cn.xjbpm.rule.engine.definition.model.StartNode;
import cn.xjbpm.rule.engine.runtime.context.ExecutionScope;
import cn.xjbpm.rule.engine.runtime.entity.ExecutionEntity;
import lombok.extern.slf4j.Slf4j;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/30
 */
@Slf4j
public class StartNodeBehavior extends BaseNodeBehavior {

    private final StartNode node;

    public StartNodeBehavior(StartNode node) {
        this.node = node;
    }


    @Override
    public void unableToComplete(ExecutionEntity executionEntity) {
        log.info("节点[{}]不满足完成条件，暂挂在此节点", node.getKey());
    }

    @Override
    public void doExecution(ExecutionEntity executionEntity, ExecutionScope executionScope) {
        log.info("执行开始节点,流程编号:{}", executionEntity.getProcessInstanceId());
    }

    @Override
    public Node getCurrentNode() {
        return this.node;
    }
}
