package com.myflow.common.utils;

import com.myflow.aviator.AviatorContext;
import com.myflow.aviator.AviatorExecutor;
import com.myflow.rule.Action;
import com.myflow.rule.enums.AssignmentType;

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
        aviatorContext.setExpression(String.format("%s=%s", action.getLeft(), action.getValues()));
        if (action.getAssignmentType() == AssignmentType.CALC) {
            aviatorContext.setExpression(String.format("%s=%s", action.getLeft(), action.getExpressionValue()));
        } else if (action.getAssignmentType() == AssignmentType.VAR) {
            aviatorContext.setExpression(String.format("%s=%s", action.getLeft(), action.getFieldValue()));
        }
        AviatorExecutor.execute(aviatorContext);
    }
}
