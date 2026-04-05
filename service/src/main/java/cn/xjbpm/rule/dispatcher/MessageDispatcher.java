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

package cn.xjbpm.rule.dispatcher;

import cn.xjbpm.rule.consumer.ProcessMessageHelper;
import cn.xjbpm.rule.filter.MessageFilterFactory;
import cn.xjbpm.rule.manager.SharedConnectionManager;
import cn.xjbpm.rule.node.enums.FilterType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 消息分发器
 * 将消息分发到所有订阅的流程，支持基于过滤规则的条件匹配分发
 *
 * @author 黄川 huchuc@vip.qq.com
 * date: 2026/03/18
 */
@Slf4j
@Component
@AllArgsConstructor
public class MessageDispatcher {

    private final MessageFilterFactory messageFilterFactory;
    private final ProcessMessageHelper processMessageHelper;
    private final SharedConnectionManager sharedConnectionManager;

    /**
     * 分发消息到所有订阅者
     * 遍历订阅者列表，对每个订阅者进行消息过滤和流程触发
     *
     * @param connectionKey   连接标识
     * @param message         消息内容
     * @param subscriberInfos 订阅者信息列表（包含过滤规则）
     */
    public void dispatch(String connectionKey, String message, List<SubscriberInfo> subscriberInfos) {
        if (message == null || subscriberInfos == null || subscriberInfos.isEmpty()) {
            log.debug("消息分发跳过: message为空或订阅者列表为空, connectionKey={}", connectionKey);
            return;
        }
        log.info("开始分发消息: connectionKey={}, 订阅者数量={}", connectionKey, subscriberInfos.size());
        for (SubscriberInfo info : subscriberInfos) {
            try {
                dispatchToSubscriber(message, info);
            } catch (Exception e) {
                log.error("分发消息到订阅者失败: ruleFlowKey={}, error={}", info.getRuleFlowKey(), e.getMessage(), e);
            }
        }
    }

    /**
     * 分发消息到单个订阅者
     * 先检查消息是否匹配过滤规则，匹配则异步触发流程执行
     *
     * @param message 消息内容
     * @param info    订阅者信息
     */
    @Async
    public void dispatchToSubscriber(String message, SubscriberInfo info) {
        if (info == null || info.getRuleFlowKey() == null) {
            log.warn("订阅者信息无效，跳过分发");
            return;
        }
        String ruleFlowKey = info.getRuleFlowKey();
        String filterRule = info.getFilterRule();
        FilterType filterType = info.getFilterType();

        if (matchesFilter(message, filterRule, filterType)) {
            log.info("消息匹配过滤规则，触发流程: ruleFlowKey={}, filterType={}", ruleFlowKey, filterType);
            processMessageHelper.processMessage(ruleFlowKey, message);
        } else {
            log.debug("消息不匹配过滤规则，跳过: ruleFlowKey={}, filterType={}, filterRule={}",
                    ruleFlowKey, filterType, filterRule);
        }
    }

    /**
     * 检查消息是否匹配过滤规则
     * 使用 MessageFilterFactory 进行匹配判断
     *
     * @param message    消息内容
     * @param filterRule 过滤规则表达式
     * @param filterType 过滤器类型
     * @return 是否匹配过滤规则，如果过滤器类型为 NONE 则返回 true
     */
    private boolean matchesFilter(String message, String filterRule, FilterType filterType) {
        try {
            return messageFilterFactory.matches(message, filterRule, filterType);
        } catch (Exception e) {
            log.error("过滤规则匹配异常: filterType={}, filterRule={}, error={}",
                    filterType, filterRule, e.getMessage(), e);
            return false;
        }
    }
}
