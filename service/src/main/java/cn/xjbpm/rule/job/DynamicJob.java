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
package cn.xjbpm.rule.job;

import cn.hutool.core.util.IdUtil;
import cn.xjbpm.rule.common.utils.JsonUtils;
import cn.xjbpm.rule.dto.ExcuteRuleFlow;
import cn.xjbpm.rule.engine.aviator.AviatorContext;
import cn.xjbpm.rule.engine.aviator.AviatorExecutor;
import cn.xjbpm.rule.engine.runtime.RuleFlowExcuteService;
import cn.xjbpm.rule.repository.entity.RuleFlowScheduledEntity;
import cn.xjbpm.rule.utils.FieldUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 */
@Slf4j
@Component
@DisallowConcurrentExecution
@AllArgsConstructor
public class DynamicJob implements Job {


    private final RuleFlowExcuteService ruleFlowExcuteService;

    @Override
    public void execute(JobExecutionContext context) {
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        Long id = jobDataMap.getLong(FieldUtil.name(RuleFlowScheduledEntity::getId));
        String ruleFlowKey = jobDataMap.getString(FieldUtil.name(RuleFlowScheduledEntity::getRuleFlowKey));
        String cronExpression = jobDataMap.getString(FieldUtil.name(RuleFlowScheduledEntity::getCronExpression));
        String requestParams = jobDataMap.getString(FieldUtil.name(RuleFlowScheduledEntity::getRequestParams));
        log.info("任务ID:{} 调度:{} 表达式:{}", id, ruleFlowKey, cronExpression);
        String realRequestParams = AviatorExecutor.evaluateAndReplace(AviatorContext.create(requestParams, null));
        ExcuteRuleFlow excuteRuleFlow = new ExcuteRuleFlow();
        excuteRuleFlow.setKey(ruleFlowKey);
        excuteRuleFlow.setRequestId(IdUtil.getSnowflakeNextIdStr());
        excuteRuleFlow.setVariables(JsonUtils.json2Obj(realRequestParams, Map.class));
        excuteRuleFlow.setAsyncExcute(true);
        ruleFlowExcuteService.startFlow(excuteRuleFlow);
    }
}