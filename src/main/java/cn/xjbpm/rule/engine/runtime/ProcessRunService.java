package cn.xjbpm.rule.engine.runtime;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.xjbpm.rule.engine.common.constant.ProcessConstant;
import cn.xjbpm.rule.engine.common.utils.JsonUtil;
import cn.xjbpm.rule.engine.common.utils.VariableTranslateUtils;
import cn.xjbpm.rule.engine.definition.model.ProcessModel;
import cn.xjbpm.rule.engine.definition.model.StartNode;
import cn.xjbpm.rule.engine.runtime.context.ProcessRuntimeContext;
import cn.xjbpm.rule.engine.runtime.entity.ExecutionEntity;
import cn.xjbpm.rule.engine.runtime.entity.ExecutionEntityImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/30
 */
@Component
@Slf4j
@AllArgsConstructor
@SuppressWarnings("all")
public class ProcessRunService {


    private final ProcessDefinitionService processDefinitionService;

    /**
     * 开始流程
     *
     * @param processRuntimeContext
     * @return
     */
    public Map<String, Object> start(ProcessRuntimeContext processRuntimeContext) {
        Assert.isTrue(StrUtil.isNotBlank(processRuntimeContext.getKey()));
        long snowflakeNextId = IdUtil.getSnowflakeNextId();
        ProcessModel processModel = processDefinitionService.getProcessModel(processRuntimeContext.getKey(), processRuntimeContext.getVersion());
        Map<String, Object> runtimeVar = new HashMap<>();
        runtimeVar.put(ProcessConstant.BUSINESS_OBJECTS, VariableTranslateUtils.translate(processModel.getBusinessObjectModels(), false, JsonUtil.obj2Json(new HashMap<>(processRuntimeContext.getVariable()))));
        ExecutionEntity executionEntity = new ExecutionEntityImpl(snowflakeNextId, runtimeVar);
        StartNode startNode = processModel.getStartNode();
        startNode.getBehavior().execution(executionEntity);
        Map<String, Object> result = new HashMap<>();
        result.put("processInstanceId", executionEntity.getProcessInstanceId());
        result.put("response", VariableTranslateUtils.translate(processModel.getBusinessObjectModels(), true, JsonUtil.obj2Json(runtimeVar.get(ProcessConstant.BUSINESS_OBJECTS))));
        return result;
    }

    /**
     * 终止流程
     *
     * @param processId
     * @param reason
     * @return
     */
    public boolean stop(Long processId, String reason) {
        throw new UnsupportedOperationException();
    }

}
