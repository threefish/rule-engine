/*
 * Copyright 2025 threefish.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.xjbpm.rule.engine.runtime;

import akka.actor.ActorSystem;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.xjbpm.rule.common.constant.ProcessConstant;
import cn.xjbpm.rule.common.utils.VariableTranslateUtils;
import cn.xjbpm.rule.custom.ProcessDefinitionService;
import cn.xjbpm.rule.dto.ExcuteRuleFlowVO;
import cn.xjbpm.rule.dto.ExcutingHistoryLogVO;
import cn.xjbpm.rule.dto.RuleFlowExcuteCompledEvent;
import cn.xjbpm.rule.engine.definition.model.ProcessModel;
import cn.xjbpm.rule.engine.runtime.actor.AkkaRuleFlowScheduler;
import cn.xjbpm.rule.engine.runtime.model.FlowContext;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationEventPublisher;
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
public class ProcessRunService implements DisposableBean {


    private final ProcessDefinitionService processDefinitionService;
    private final ApplicationEventPublisher applicationEventPublisher;

    private final ActorSystem actorSystem = ActorSystem.create("rule-engine");


    @Override
    public void destroy() throws Exception {
        actorSystem.terminate();
    }

    /**
     * 开始流程
     *
     * @param createProcessRequest
     * @return
     */
    public ExcuteRuleFlowVO.Response excute(ExcuteRuleFlowVO.Request createProcessRequest) {
        Assert.isTrue(StrUtil.isNotBlank(createProcessRequest.getKey()), "key不能为空");
        ProcessModel processModel = processDefinitionService.getProcessModel(createProcessRequest.getKey(), createProcessRequest.getVersion());
        return doExcute(processModel, createProcessRequest.getVariables(), createProcessRequest.getRequestId());
    }

    private ExcuteRuleFlowVO.Response doExcute(ProcessModel processModel, Map variables, String requestId) {
        ExcuteRuleFlowVO.Response processInstance = new ExcuteRuleFlowVO.Response();
        processInstance.setRequestId(requestId);
        processInstance.setId(IdUtil.getSnowflakeNextId());
        processInstance.setRuleFlowKey(processModel.getKey());
        Map<String, Object> runtimeVar = new HashMap<>();
        runtimeVar.put(ProcessConstant.BUSINESS_OBJECTS, VariableTranslateUtils.translate(processModel.getBusinessObjectModels(), false, variables));
        FlowContext flowContext = new FlowContext(runtimeVar);
        long startTime = System.currentTimeMillis();
        try {
            AkkaRuleFlowScheduler scheduler = new AkkaRuleFlowScheduler(actorSystem);
            scheduler.startFlow(processModel, flowContext);
            processInstance.setSuccess(true);
        } catch (Exception e) {
            processInstance.setErrorMessage(StrUtil.subPre(e.getMessage(), 100));
            processInstance.setSuccess(false);
            log.error("流程执行出错：{}", e.getMessage(), e);
        } finally {
            processInstance.setNodeExcutions(flowContext.getNodeExcutions());
            processInstance.setTimeConsuming((System.currentTimeMillis() - startTime));
            processInstance.setTraceLogs(flowContext.getTraceLogs());
        }
        Map businessVariables = (Map) flowContext.getVariable().get(ProcessConstant.BUSINESS_OBJECTS);
        Map<String, Object> response = VariableTranslateUtils.translate(processModel.getBusinessObjectModels(), true, businessVariables);
        processInstance.setResponse(response);
        if (StrUtil.isNotBlank(processModel.getKey())) {
            applicationEventPublisher.publishEvent(RuleFlowExcuteCompledEvent.create(ExcutingHistoryLogVO.create(processInstance, variables, processModel.getOriginalJson())));
        }
        return processInstance;
    }

    public ExcuteRuleFlowVO.Response testExcute(ExcuteRuleFlowVO.Request createProcessRequest) {
        ExcuteRuleFlowVO.Response processInstance = new ExcuteRuleFlowVO.Response();
        processInstance.setRequestId(createProcessRequest.getRequestId());
        ProcessModel processModel = processDefinitionService.convertToModel(createProcessRequest.getContent());
        return doExcute(processModel, createProcessRequest.getVariables(), createProcessRequest.getRequestId());
    }

}