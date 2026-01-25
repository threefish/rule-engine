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
import cn.xjbpm.rule.repository.entity.ExcuteLogEntity;
import cn.xjbpm.rule.service.ExcuteLogService;
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

    private final ExcuteLogService ruleFlowExcuteLogService;

    /**
     * 监听规则流执行完成事件
     * 异步存储执行结果
     * 当前方式是性能瓶颈点：优化方向考虑发送到消息队列，再进行消费
     *
     * @param event
     */
    @Override
    @Async
    public void onApplicationEvent(RuleFlowExcuteCompledEvent event) {
        log.info("监听到规则流执行完成事件：{}", event.getData().getId());
        try {
            ExcutingHistoryLogVO data = event.getData();
            ExcuteLogEntity logEntity = new ExcuteLogEntity();
            if (Objects.nonNull(data.getRetryOriginId())) {
                logEntity.setId(data.getRetryOriginId());
            } else {
                logEntity.setId(data.getId());
                logEntity.setRuleFlowKey(data.getRuleFlowKey());
                logEntity.setRequestId(data.getRequestId());
            }
            logEntity.setTimeConsuming(data.getTimeConsuming());
            logEntity.setErrorMessage(data.getErrorMessage());
            logEntity.setStatus(data.getStatus());
            logEntity.setContent(JsonUtils.obj2Json(data));
            ruleFlowExcuteLogService.save(logEntity);
        } catch (Exception e) {
            log.error("存储执行日志出错：{}", e.getMessage(), e);
        }
    }
}
