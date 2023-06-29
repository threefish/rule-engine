package com.myflow.runtime;

import cn.hutool.core.util.IdUtil;
import com.myflow.definition.model.ProcessModel;
import com.myflow.definition.model.StartNode;
import com.myflow.runtime.behavior.NodeBehavior;
import com.myflow.runtime.context.ProcessRuntimeContext;
import com.myflow.runtime.entity.ExecutionEntity;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/30
 */
public class DefaultProcessRunService implements ProcessRunService {

    @Override
    public Long start(ProcessRuntimeContext processRuntimeContext) {
        long snowflakeNextId = IdUtil.getSnowflakeNextId();
        ExecutionEntity executionEntity = new ExecutionEntity(snowflakeNextId, processRuntimeContext.getVariable());
        ProcessModel processModel = processRuntimeContext.getProcessModel();
        StartNode startNode = processModel.getStartNode();
        NodeBehavior behavior = startNode.getBehavior();
        behavior.execution(executionEntity);
        return executionEntity.getProcessInstanceId();
    }

    @Override
    public boolean stop(Long processId, String reason) {
        throw new UnsupportedOperationException();
    }

}
