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

package cn.xjbpm.rule.common.utils;

import cn.hutool.core.date.DateUtil;
import cn.xjbpm.rule.engine.aviator.AviatorContext;
import cn.xjbpm.rule.engine.aviator.AviatorExecutor;
import cn.xjbpm.rule.engine.rule.Action;
import cn.xjbpm.rule.engine.rule.enums.AssignmentType;
import cn.xjbpm.rule.engine.rule.enums.VariableType;

import java.util.Arrays;
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
        if (action.getAssignmentType() == AssignmentType.CALC) {
            setCalcValue(action, variable);
        } else if (action.getAssignmentType() == AssignmentType.VAR) {
            setVarValue(action, variable);
        } else if (action.getAssignmentType() == AssignmentType.FIXED) {
            setFixedValue(action, variable);
        }
    }

    private static void setCalcValue(Action action, Map<String, Object> variable) {
        AviatorContext aviatorContext = AviatorContext.builder().cached(true).env(variable).build();
        aviatorContext.setExpression(String.format("%s=(%s)", action.getLeft(), action.getExpressionValue()));
        AviatorExecutor.execute(aviatorContext);
    }

    private static void setVarValue(Action action, Map<String, Object> variable) {
        AviatorContext aviatorContext = AviatorContext.builder().cached(true).env(variable).build();
        aviatorContext.setExpression(String.format("%s=(%s)", action.getLeft(), action.getFieldValue()));
        AviatorExecutor.execute(aviatorContext);
    }

    private static void setFixedValue(Action action, Map<String, Object> variable) {
        VariableType varType = action.getVarType();
        if (Arrays.asList(VariableType.STRING, VariableType.NUMBER, VariableType.BOOLEAN).contains(varType)) {
            VariableUtils.setPathVariableByValue(action.getLeft(), action.getValue(), variable);
            return;
        }
        if (VariableType.DATE_TIME == varType) {
            VariableUtils.setPathVariableByValue(action.getLeft(), DateUtil.parse(String.valueOf(action.getValue()), "yyyy-MM-dd HH:mm:ss"), variable);
            return;
        }
        if (VariableType.DATE == varType) {
            VariableUtils.setPathVariableByValue(action.getLeft(), DateUtil.parse(String.valueOf(action.getValue()), "yyyy-MM-dd"), variable);
            return;
        }
        throw new RuntimeException(String.format("[%s]类型变量不支持赋值!", varType));
    }
}