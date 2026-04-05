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

import cn.xjbpm.rule.common.utils.JsonUtils;
import cn.xjbpm.rule.engine.aviator.AviatorContext;
import cn.xjbpm.rule.engine.aviator.AviatorExecutor;
import cn.xjbpm.rule.node.enums.FilterType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Spring EL 表达式消息过滤器实现
 * <p>
 * 使用 Aviator 表达式对消息进行过滤
 * 支持表达式如: type == 'payment' && amount<=100
 *
 * @author 黄川 huchuc@vip.qq.com
 * date: 2026/03/18
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AviatorMessageFilter implements MessageFilter {


    @Override
    public boolean matches(String message, String filterRule) {
        if (message == null || message.isBlank()) {
            log.warn("消息内容为空，无法进行 Aviator 过滤");
            return false;
        }
        if (filterRule == null || filterRule.isBlank()) {
            return true;
        }
        try {
            Map<String, Object> messageMap = JsonUtils.json2Obj(message, Map.class);
            Object result = AviatorExecutor.executeBoolean(AviatorContext.create(filterRule, messageMap));
            if (result instanceof Boolean) {
                return (Boolean) result;
            }
            log.warn("Aviator 表达式执行结果不是布尔类型，实际类型: {}, 结果: {}",
                    result != null ? result.getClass().getName() : "null", result);
            return result != null;
        } catch (Exception e) {
            log.error("Aviator 表达式执行失败，表达式: {}, 错误: {}", filterRule, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.AVIATOREL;
    }


}
