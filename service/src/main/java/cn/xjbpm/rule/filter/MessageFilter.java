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

/**
 * 消息过滤器接口
 * <p>
 * 定义消息过滤的标准行为，支持多种过滤方式
 *
 * @author 黄川 huchuc@vip.qq.com
 * date: 2026/03/18
 */
public interface MessageFilter {

    /**
     * 判断消息是否匹配过滤规则
     *
     * @param message    消息内容
     * @param filterRule 过滤规则表达式
     * @return 是否匹配过滤规则
     */
    boolean matches(String message, String filterRule);

    /**
     * 获取过滤器类型
     *
     * @return 过滤器类型枚举
     */
    FilterType getFilterType();
}
