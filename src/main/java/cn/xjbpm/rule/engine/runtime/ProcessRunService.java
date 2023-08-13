package cn.xjbpm.rule.engine.runtime;

import cn.hutool.core.util.StrUtil;
import cn.xjbpm.rule.engine.adapter.ProcessDefinitionAdapter;
import cn.xjbpm.rule.engine.adapter.ProcessInstanceAdapter;
import cn.xjbpm.rule.engine.adapter.persistence.po.ProcessDefinitionEntity;
import cn.xjbpm.rule.engine.adapter.persistence.po.ProcessInstanceEntity;
import cn.xjbpm.rule.engine.common.constant.ProcessConstant;
import cn.xjbpm.rule.engine.common.utils.IdGenerUtils;
import cn.xjbpm.rule.engine.common.utils.VariableTranslateUtils;
import cn.xjbpm.rule.engine.definition.model.ProcessModel;
import cn.xjbpm.rule.engine.definition.model.StartNode;
import cn.xjbpm.rule.engine.model.CreateProcessRequest;
import cn.xjbpm.rule.engine.runtime.behavior.NodeBehavior;
import cn.xjbpm.rule.engine.runtime.context.ProcessContextHolder;
import cn.xjbpm.rule.engine.runtime.context.ProcessRuntimeContext;
import cn.xjbpm.rule.engine.runtime.entity.ProcessInstance;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.enhance.incrementer.IdentifierGenerator;
import org.springframework.beans.BeanUtils;
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


    private final ProcessDefinitionAdapter processDefinitionService;
    private final ProcessInstanceAdapter processInstanceAdapter;
    private final IdentifierGenerator identifierGenerator;

    /**
     * 开始流程
     *
     * @param createProcessRequest
     * @return
     */
//    @Transactional(rollbackFor = Exception.class)
    public ProcessInstance start(CreateProcessRequest createProcessRequest) {
        try {
            Assert.isTrue(StrUtil.isNotBlank(createProcessRequest.getKey()));
            long snowflakeNextId = IdGenerUtils.nextGlobalId();

            ProcessDefinitionEntity processDefinition = processDefinitionService.getProcessDefinition(createProcessRequest.getKey(), createProcessRequest.getVersion());
            ProcessModel processModel = processDefinitionService.getProcessModel(processDefinition);
            Map<String, Object> runtimeVar = new HashMap<>();
            runtimeVar.put(ProcessConstant.BUSINESS_OBJECTS, VariableTranslateUtils.translate(processModel.getBusinessObjectModels(), false, createProcessRequest.getVariable()));

            ProcessRuntimeContext processRuntimeContext = ProcessContextHolder.createContext(snowflakeNextId, createProcessRequest.getEnvironment(), processModel, runtimeVar);

            ProcessInstanceEntity processInstanceEntity = processInstanceAdapter.createProcessInstance(snowflakeNextId, processDefinition, runtimeVar);

            StartNode startNode = processModel.getStartNode();
            NodeBehavior behavior = startNode.getBehavior();
            behavior.execution(processInstanceEntity);

            processInstanceEntity.setProcessStatus(processRuntimeContext.getProcessStatus());
            processInstanceAdapter.updateProcessInstance(processInstanceEntity);
            ProcessInstance processInstance = new ProcessInstance();
            BeanUtils.copyProperties(processInstanceEntity, processInstance);
            Map<String, Object> response = VariableTranslateUtils.translate(processModel.getBusinessObjectModels(), true, (Map) runtimeVar.get(ProcessConstant.BUSINESS_OBJECTS));
            processInstance.setResponse(response);
            return processInstance;
        } finally {
            ProcessContextHolder.destroy();
        }

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
