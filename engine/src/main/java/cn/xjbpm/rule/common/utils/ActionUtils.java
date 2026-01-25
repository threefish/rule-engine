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

import cn.xjbpm.rule.engine.aviator.AviatorContext;
import cn.xjbpm.rule.engine.aviator.AviatorExecutor;
import cn.xjbpm.rule.engine.rule.Action;
import cn.xjbpm.rule.engine.rule.enums.AssignmentType;

import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/6/30
 */
public class ActionUtils {
    /**
     * 赋值
     *
     * @param action
     * @param variable
     */
    public static void assignment(Action action, Map<String, Object> variable) {
        AviatorContext aviatorContext = AviatorContext.builder().cached(true).env(variable).build();
        aviatorContext.setExpression(String.format("%s=%s", action.getLeft(), action.getValue()));
        if (action.getAssignmentType() == AssignmentType.CALC) {
            aviatorContext.setExpression(String.format("%s=%s", action.getLeft(), action.getExpressionValue()));
        } else if (action.getAssignmentType() == AssignmentType.VAR) {
            aviatorContext.setExpression(String.format("%s=%s", action.getLeft(), action.getFieldValue()));
        }
        AviatorExecutor.execute(aviatorContext);
    }
}