package cn.xjbpm.rule.engine.runtime;

import cn.hutool.core.util.IdUtil;
import cn.xjbpm.rule.engine.definition.model.ProcessModel;
import cn.xjbpm.rule.engine.definition.model.StartNode;
import cn.xjbpm.rule.engine.runtime.context.ProcessRuntimeContext;
import cn.xjbpm.rule.engine.runtime.entity.ExecutionEntity;
import cn.xjbpm.rule.engine.runtime.entity.ExecutionEntityImpl;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/30
 */
public class DefaultProcessRunService implements ProcessRunService {

    @Override
    public Long start(ProcessRuntimeContext processRuntimeContext) {
        long snowflakeNextId = IdUtil.getSnowflakeNextId();
        ExecutionEntity executionEntity = new ExecutionEntityImpl(snowflakeNextId, processRuntimeContext.getVariable());
        ProcessModel processModel = processRuntimeContext.getProcessModel();
        StartNode startNode = processModel.getStartNode();
        startNode.getBehavior().execution(executionEntity);
        return executionEntity.getProcessInstanceId();
    }

    @Override
    public boolean stop(Long processId, String reason) {
        throw new UnsupportedOperationException();
    }

}
