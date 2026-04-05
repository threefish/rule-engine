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

package cn.xjbpm.rule.filter;

import cn.xjbpm.rule.node.enums.FilterType;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息过滤器工厂
 * <p>
 * 根据过滤器类型获取对应的过滤器实现
 *
 * @author 黄川 huchuc@vip.qq.com
 * date: 2026/03/18
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageFilterFactory {

    private final List<MessageFilter> messageFilterList;
    private final Map<FilterType, MessageFilter> filters = new HashMap<>();

    /**
     * 初始化过滤器映射
     */
    @PostConstruct
    public void init() {
        for (MessageFilter filter : messageFilterList) {
            filters.put(filter.getFilterType(), filter);
            log.info("注册消息过滤器: {} -> {}", filter.getFilterType(), filter.getClass().getSimpleName());
        }
    }

    /**
     * 根据过滤器类型获取对应的过滤器
     *
     * @param type 过滤器类型
     * @return 过滤器实现，如果类型为 NONE 或未找到则返回 null
     */
    public MessageFilter getFilter(FilterType type) {
        if (type == null || type == FilterType.NONE) {
            return null;
        }
        MessageFilter filter = filters.get(type);
        if (filter == null) {
            log.warn("未找到过滤器类型: {} 对应的实现", type);
        }
        return filter;
    }

    /**
     * 使用指定类型的过滤器判断消息是否匹配过滤规则
     *
     * @param message    消息内容
     * @param filterRule 过滤规则表达式
     * @param filterType 过滤器类型
     * @return 是否匹配过滤规则，如果过滤器类型为 NONE 则返回 true
     */
    public boolean matches(String message, String filterRule, FilterType filterType) {
        if (filterType == null || filterType == FilterType.NONE) {
            return true;
        }
        MessageFilter filter = getFilter(filterType);
        if (filter == null) {
            log.warn("过滤器类型 {} 未找到对应实现，默认返回 true", filterType);
            return true;
        }
        return filter.matches(message, filterRule);
    }
}
