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
package cn.xjbpm.rule.listener;

import cn.xjbpm.rule.common.utils.JsonUtils;
import cn.xjbpm.rule.dto.ExcutingHistoryLogVO;
import cn.xjbpm.rule.event.RuleFlowExcuteCompledEvent;
import cn.xjbpm.rule.repository.entity.RuleFlowExcuteLogEntity;
import cn.xjbpm.rule.service.RuleFlowExcuteLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author 黄川 huchuc@vip.qq.com
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RuleFlowExcuteCompledEventListener implements ApplicationListener<RuleFlowExcuteCompledEvent> {

    private final RuleFlowExcuteLogService ruleFlowExcuteLogService;

    /**
     * 监听流程执行完成事件
     * 异步存储执行结果
     *
     * @param event
     */
    @Override
    @Async
    public void onApplicationEvent(RuleFlowExcuteCompledEvent event) {
        try {
            ExcutingHistoryLogVO data = event.getData();
            RuleFlowExcuteLogEntity logEntity = new RuleFlowExcuteLogEntity();
            if (Objects.nonNull(data.getRetryOriginId())) {
                logEntity = ruleFlowExcuteLogService.findById(data.getRetryOriginId());
                if (Objects.nonNull(data.getRetryOriginId())) {
                    log.info("更新执行日志 key:{} ID:{}", data.getRuleFlowKey(), data.getRetryOriginId());
                    logEntity.setTimeConsuming(data.getTimeConsuming());
                    logEntity.setErrorMessage(data.getErrorMessage());
                    logEntity.setSuccess(data.getSuccess());
                    logEntity.setContent(JsonUtils.obj2Json(data));
                } else {
                    logEntity.setId(data.getId());
                    logEntity.setRuleFlowKey(data.getRuleFlowKey());
                    logEntity.setRequestId(data.getRequestId());
                }
            } else {
                log.info("存储执行日志 key:{} ID:{}", data.getRuleFlowKey(), data.getId());
                logEntity.setId(data.getId());
                logEntity.setRuleFlowKey(data.getRuleFlowKey());
                logEntity.setRequestId(data.getRequestId());
                logEntity.setTimeConsuming(data.getTimeConsuming());
                logEntity.setErrorMessage(data.getErrorMessage());
                logEntity.setSuccess(data.getSuccess());
                logEntity.setContent(JsonUtils.obj2Json(data));
            }
            ruleFlowExcuteLogService.save(logEntity);
        } catch (Exception e) {
            log.error("存储执行日志出错：{}", e.getMessage(), e);
        }
    }
}
