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
import cn.xjbpm.rule.common.constant.RuleFlowConstant;
import cn.xjbpm.rule.common.utils.VariableTranslateUtils;
import cn.xjbpm.rule.custom.RuleFlowModelCacheService;
import cn.xjbpm.rule.dto.ExcuteRuleFlow;
import cn.xjbpm.rule.dto.ExcuteRuleFlowResult;
import cn.xjbpm.rule.dto.ExcutingHistoryLogVO;
import cn.xjbpm.rule.engine.definition.model.RuleFlowModel;
import cn.xjbpm.rule.engine.runtime.actor.AkkaRuleFlowScheduler;
import cn.xjbpm.rule.engine.runtime.model.ExecutStatus;
import cn.xjbpm.rule.engine.runtime.model.FlowContext;
import cn.xjbpm.rule.engine.runtime.model.NodeExcution;
import cn.xjbpm.rule.event.RuleFlowExcuteCompledEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/30
 */
@Component
@Slf4j
@AllArgsConstructor
public class RuleFlowExcuteService implements DisposableBean {


    private final RuleFlowModelCacheService ruleFlowModelCacheService;
    private final ApplicationEventPublisher applicationEventPublisher;

    private final ActorSystem actorSystem = ActorSystem.create("rule-engine");


    @Override
    public void destroy() {
        actorSystem.terminate();
    }

    /**
     * 开始流程
     *
     * @param request
     * @return
     */
    public ExcuteRuleFlowResult startFlow(ExcuteRuleFlow request) {
        Assert.isTrue(StrUtil.isNotBlank(request.getKey()), "规则编码不能为空");
        RuleFlowModel processModel;
        if (StringUtils.hasText(request.getContent())) {
            processModel = ruleFlowModelCacheService.convertToModel(request.getContent());
        } else {
            processModel = ruleFlowModelCacheService.getModel(request.getKey());
        }
        ExcuteRuleFlowResult processInstance = new ExcuteRuleFlowResult();
        processInstance.setRequestId(request.getRequestId());
        processInstance.setId(IdUtil.getSnowflakeNextId());
        processInstance.setRuleFlowKey(processModel.getKey());
        Map<String, Object> runtimeVar = new HashMap<>();
        runtimeVar.put(RuleFlowConstant.BUSINESS_OBJECTS, VariableTranslateUtils.translate(processModel.getBusinessObjectModels(), false, request.getVariables()));
        FlowContext flowContext = new FlowContext(runtimeVar);
        long startTime = System.currentTimeMillis();
        AkkaRuleFlowScheduler scheduler = new AkkaRuleFlowScheduler(actorSystem);
        if (request.isAsyncExcute()) {
            scheduler.startFlowAsync(processModel, flowContext, request.getSkipNodeIds(), failure -> {
                if (Objects.isNull(failure)) {
                    processInstance.setSuccess(true);
                } else {
                    handFailure(processInstance, flowContext, failure);
                }
                processInstance.setNodeExcutions(flowContext.getNodeExcutions());
                processInstance.setTimeConsuming((System.currentTimeMillis() - startTime));
                processInstance.setTraceLogs(flowContext.getTraceLogs());
                doComplete(flowContext, processModel, processInstance, request);
            });
        } else {
            try {
                scheduler.startFlow(processModel, flowContext, request.getSkipNodeIds());
                processInstance.setSuccess(true);
            } catch (Exception e) {
                handFailure(processInstance, flowContext, e);
            } finally {
                processInstance.setNodeExcutions(flowContext.getNodeExcutions());
                processInstance.setTimeConsuming((System.currentTimeMillis() - startTime));
                processInstance.setTraceLogs(flowContext.getTraceLogs());
            }
            doComplete(flowContext, processModel, processInstance, request);
        }
        return processInstance;
    }

    /**
     * 处理失败
     *
     * @param excuteRuleFlowResult
     * @param flowContext
     * @param failure
     */
    private void handFailure(ExcuteRuleFlowResult excuteRuleFlowResult, FlowContext flowContext, Throwable failure) {
        List<NodeExcution> failedExecutions = flowContext.getNodeExcutions().values().stream()
                .filter(node -> node.getStatus() == ExecutStatus.FAILURE)
                .collect(Collectors.toList());
        if (!failedExecutions.isEmpty()) {
            NodeExcution nodeExcution = failedExecutions.get(0);
            excuteRuleFlowResult.setErrorMessage(StrUtil.subPre(String.format("节点:[%s] 异常描述:%s", nodeExcution.getId(), nodeExcution.getErrorMessage()), 100));
        } else {
            excuteRuleFlowResult.setErrorMessage(StrUtil.subPre(failure.getMessage(), 100));
        }
        excuteRuleFlowResult.setSuccess(false);
        log.error("流程执行出错：{}", failure.getMessage(), failure);
    }

    /**
     * 完成
     *
     * @param flowContext
     * @param processModel
     * @param processInstance
     * @param request
     */
    private void doComplete(FlowContext flowContext, RuleFlowModel processModel, ExcuteRuleFlowResult processInstance, ExcuteRuleFlow request) {
        Map businessVariables = (Map) flowContext.getVariable().get(RuleFlowConstant.BUSINESS_OBJECTS);
        Map<String, Object> response = VariableTranslateUtils.translate(processModel.getBusinessObjectModels(), true, businessVariables);
        processInstance.setResponse(response);
        if (StrUtil.isNotBlank(processModel.getKey())) {
            applicationEventPublisher.publishEvent(RuleFlowExcuteCompledEvent.create(ExcutingHistoryLogVO.create(processInstance, request.getVariables(), processModel.getOriginalJson(), request.getRetryOriginId())));
        }
    }

}