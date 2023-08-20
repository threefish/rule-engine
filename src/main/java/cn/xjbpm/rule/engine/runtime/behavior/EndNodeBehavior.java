package cn.xjbpm.rule.engine.runtime.behavior;

import cn.xjbpm.rule.engine.adapter.AdapterContextHolder;
import cn.xjbpm.rule.engine.common.enums.ProcessStatusEnum;
import cn.xjbpm.rule.engine.definition.model.EndNode;
import cn.xjbpm.rule.engine.definition.model.Node;
import cn.xjbpm.rule.engine.runtime.context.ExecutionScope;
import cn.xjbpm.rule.engine.runtime.context.ProcessContextHolder;
import cn.xjbpm.rule.engine.runtime.entity.ExecutionEntity;
import lombok.extern.slf4j.Slf4j;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/30
 */
@Slf4j
public class EndNodeBehavior implements NodeBehavior {

    private final EndNode node;

    public EndNodeBehavior(EndNode node) {
        this.node = node;
    }


    @Override
    public void execution(ExecutionEntity executionEntity) {
        this.execution(executionEntity, null);
    }

    @Override
    public void execution(ExecutionEntity executionEntity, ExecutionScope executionScope) {
        log.info("流程结束 {}", node.getKey());
        ProcessContextHolder.getContext().setProcessStatus(ProcessStatusEnum.COMPLETED);
        AdapterContextHolder.nodeExecutionAdapter.updateExecution2Completed(executionEntity);
    }

    @Override
    public void leave(ExecutionEntity executionEntity) {

    }

    @Override
    public void leave(ExecutionEntity executionEntity, ExecutionScope executionScope) {

    }

}
