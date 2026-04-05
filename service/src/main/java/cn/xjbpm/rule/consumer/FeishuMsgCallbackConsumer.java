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
import cn.xjbpm.rule.dispatcher.MessageDispatcher;
import cn.xjbpm.rule.dispatcher.SubscriberInfo;
import cn.xjbpm.rule.dispatcher.TriggerSubscriptionManager;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * 飞书消息回调消费者
 * 支持连接共享机制，将消息分发到所有订阅者
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Slf4j
public class FeishuMsgCallbackConsumer {

    private final ProcessMessageHelper processMessageService;
    private final String ruleFlowKey;
    private final String connectionKey;
    private final TriggerSubscriptionManager triggerSubscriptionManager;
    private final MessageDispatcher messageDispatcher;
    private final boolean sharedMode;

    /**
     * 构造函数（兼容旧模式）
     *
     * @param processMessageService 消息处理服务
     * @param ruleFlowKey           规则流标识
     */
    public FeishuMsgCallbackConsumer(ProcessMessageHelper processMessageService, String ruleFlowKey) {
        this.processMessageService = processMessageService;
        this.ruleFlowKey = ruleFlowKey;
        this.connectionKey = null;
        this.triggerSubscriptionManager = null;
        this.messageDispatcher = null;
        this.sharedMode = false;
    }

    /**
     * 构造函数（共享连接模式）
     *
     * @param processMessageService      消息处理服务
     * @param connectionKey              连接标识
     * @param triggerSubscriptionManager 订阅管理器
     * @param messageDispatcher          消息分发器
     */
    public FeishuMsgCallbackConsumer(ProcessMessageHelper processMessageService,
                                     String connectionKey,
                                     TriggerSubscriptionManager triggerSubscriptionManager,
                                     MessageDispatcher messageDispatcher) {
        this.processMessageService = processMessageService;
        this.ruleFlowKey = null;
        this.connectionKey = connectionKey;
        this.triggerSubscriptionManager = triggerSubscriptionManager;
        this.messageDispatcher = messageDispatcher;
        this.sharedMode = true;
    }

    /**
     * 处理飞书消息回调
     *
     * @param request 回调请求数据
     */
    public void handleCallback(Map<String, Object> request) {
        try {
            String message = JsonUtils.obj2Json(request);
            if (sharedMode) {
                log.info("飞书收到消息: connectionKey={}, message={}", connectionKey, message);
                List<SubscriberInfo> subscribers = triggerSubscriptionManager.getSubscribers(connectionKey);
                messageDispatcher.dispatch(connectionKey, message, subscribers);
            } else {
                log.info("飞书收到消息: ruleFlowKey={}, message={}", ruleFlowKey, message);
                processMessageService.processMessage(ruleFlowKey, message);
            }
        } catch (Exception e) {
            log.error("处理飞书消息失败: {}, error={}", sharedMode ? "connectionKey=" + connectionKey : "ruleFlowKey=" + ruleFlowKey, e.getMessage(), e);
        }
    }
}
