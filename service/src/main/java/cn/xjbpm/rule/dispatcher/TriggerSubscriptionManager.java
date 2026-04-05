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

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 触发器订阅管理器
 * 管理触发器订阅关系，存储每个连接的订阅者信息（包含过滤规则）
 *
 * @author 黄川 huchuc@vip.qq.com
 * date: 2026/03/18
 */
@Slf4j
@Component
public class TriggerSubscriptionManager {

    /**
     * 订阅映射表
     * connectionKey -> List<SubscriberInfo>
     */
    private final Map<String, List<SubscriberInfo>> subscriptionMap = new ConcurrentHashMap<>();

    /**
     * 添加订阅
     * 将订阅者信息添加到指定连接的订阅列表中
     *
     * @param connectionKey  连接标识
     * @param subscriberInfo 订阅者信息
     */
    public void addSubscription(String connectionKey, SubscriberInfo subscriberInfo) {
        if (connectionKey == null || subscriberInfo == null) {
            log.warn("添加订阅失败: connectionKey或subscriberInfo为空");
            return;
        }
        subscriptionMap.computeIfAbsent(connectionKey, k -> new ArrayList<>())
                .add(subscriberInfo);
        log.info("添加订阅: connectionKey={}, ruleFlowKey={}, filterType={}",
                connectionKey, subscriberInfo.getRuleFlowKey(), subscriberInfo.getFilterType());
    }

    /**
     * 移除订阅
     * 从指定连接的订阅列表中移除对应的订阅者
     *
     * @param connectionKey 连接标识
     * @param ruleFlowKey   规则流标识
     */
    public void removeSubscription(String connectionKey, String ruleFlowKey) {
        if (connectionKey == null || ruleFlowKey == null) {
            log.warn("移除订阅失败: connectionKey或ruleFlowKey为空");
            return;
        }
        List<SubscriberInfo> subscribers = subscriptionMap.get(connectionKey);
        if (subscribers != null) {
            boolean removed = subscribers.removeIf(info -> ruleFlowKey.equals(info.getRuleFlowKey()));
            if (removed) {
                log.info("移除订阅: connectionKey={}, ruleFlowKey={}", connectionKey, ruleFlowKey);
            }
            if (subscribers.isEmpty()) {
                subscriptionMap.remove(connectionKey);
                log.info("连接订阅列表为空，移除连接映射: connectionKey={}", connectionKey);
            }
        }
    }

    /**
     * 获取连接的所有订阅者
     *
     * @param connectionKey 连接标识
     * @return 订阅者信息列表，如果不存在则返回空列表
     */
    public List<SubscriberInfo> getSubscribers(String connectionKey) {
        if (connectionKey == null) {
            return new ArrayList<>();
        }
        List<SubscriberInfo> subscribers = subscriptionMap.get(connectionKey);
        return subscribers != null ? new ArrayList<>(subscribers) : new ArrayList<>();
    }

    /**
     * 清除连接的所有订阅
     *
     * @param connectionKey 连接标识
     */
    public void clearSubscriptions(String connectionKey) {
        if (connectionKey == null) {
            return;
        }
        List<SubscriberInfo> removed = subscriptionMap.remove(connectionKey);
        if (removed != null) {
            log.info("清除连接的所有订阅: connectionKey={}, 移除订阅数={}", connectionKey, removed.size());
        }
    }

    /**
     * 检查连接是否有订阅者
     *
     * @param connectionKey 连接标识
     * @return 是否有订阅者
     */
    public boolean hasSubscribers(String connectionKey) {
        if (connectionKey == null) {
            return false;
        }
        List<SubscriberInfo> subscribers = subscriptionMap.get(connectionKey);
        return subscribers != null && !subscribers.isEmpty();
    }

    /**
     * 获取当前订阅连接数量
     *
     * @return 订阅连接数量
     */
    public int getSubscriptionCount() {
        return subscriptionMap.size();
    }

    /**
     * 清理所有订阅
     */
    public void clear() {
        subscriptionMap.clear();
        log.info("已清理所有订阅映射");
    }
}
