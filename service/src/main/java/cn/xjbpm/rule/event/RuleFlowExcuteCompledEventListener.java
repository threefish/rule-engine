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
package cn.xjbpm.rule.event;

import cn.xjbpm.rule.dto.ExcuteRuleFlowVO;
import cn.xjbpm.rule.dto.RuleFlowExcuteCompledEvent;
import cn.xjbpm.rule.service.RuleFlowExcuteLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

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
            ExcuteRuleFlowVO.Response data = event.getData();
            log.info("存储执行日志 key:{} ID:{}", data.getRuleFlowKey(), data.getId());
            ruleFlowExcuteLogService.save(data);
        } catch (Exception e) {
            log.error("存储执行日志出错：{}", e.getMessage(), e);
        }
    }
}
