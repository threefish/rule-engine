/**
 * Copyright 2025 threefish.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.xjbpm.rule.consumer;

import cn.xjbpm.rule.common.utils.JsonUtils;
import com.dingtalk.open.app.api.callback.OpenDingTalkCallbackListener;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 钉钉消息回调消费者
 */
@Slf4j
public class DingtalkMsgCallbackConsumer implements OpenDingTalkCallbackListener<Map, Map> {

    private final ProcessMessageHelper processMessageService;

    private final String ruleFlowKey;

    public DingtalkMsgCallbackConsumer(ProcessMessageHelper processMessageService, String ruleFlowKey) {
        this.processMessageService = processMessageService;
        this.ruleFlowKey = ruleFlowKey;
    }

    @Override
    public Map execute(Map request) {
        try {
            log.info("钉钉收到消息: ruleFlowKey={}, message={}", ruleFlowKey, JsonUtils.obj2Json(request));
            // 处理消息并触发规则流
            processMessageService.processMessage(ruleFlowKey, JsonUtils.obj2Json(request));
        } catch (Exception e) {
            log.error("处理钉钉消息失败: ruleFlowKey={}, error={}", ruleFlowKey, e.getMessage(), e);
        }
        return request;
    }
}