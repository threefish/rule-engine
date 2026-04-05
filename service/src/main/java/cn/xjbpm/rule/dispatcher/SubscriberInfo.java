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

import cn.xjbpm.rule.node.enums.FilterType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订阅者信息
 * 包含流程标识和过滤规则
 *
 * @author 黄川 huchuc@vip.qq.com
 * date: 2026/03/18
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubscriberInfo {

    /**
     * 规则流唯一标识
     */
    private String ruleFlowKey;

    /**
     * 消息过滤规则
     */
    private String filterRule;

    /**
     * 过滤类型
     */
    private FilterType filterType;

}
