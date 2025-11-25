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
package cn.xjbpm.rule.common.utils;

import cn.hutool.core.util.StrUtil;
import cn.xjbpm.rule.engine.aviator.AviatorContext;
import cn.xjbpm.rule.engine.aviator.AviatorExecutor;
import cn.xjbpm.rule.engine.rule.Rule;

import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/30
 * 条件判断工具类
 */
public class ConditionUtil {
    /**
     * 决定表达式
     *
     * @param rule
     * @param variables
     * @return
     */
    public static boolean resolve(Rule rule, Map<String, Object> variables) {
        if (rule == null) {
            return true;
        }
        String expressionCacheString = rule.getExpressionCacheString();
        return resolve(expressionCacheString, variables);
    }

    /**
     * 决定表达式
     *
     * @param expression
     * @param variables
     * @return
     */
    public static boolean resolve(String expression, Map<String, Object> variables) {
        if (StrUtil.isBlank(expression)) {
            return true;
        }
        return AviatorExecutor.executeBoolean(AviatorContext.create(expression, variables));
    }

}