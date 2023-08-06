package cn.xjbpm.rule.engine.runtime.util;

import cn.hutool.core.util.StrUtil;
import cn.xjbpm.rule.engine.aviator.AviatorContext;
import cn.xjbpm.rule.engine.aviator.AviatorExecutor;
import cn.xjbpm.rule.engine.runtime.entity.ExecutionEntity;

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
     * @param expression
     * @param variables
     * @return
     */
    public static boolean resolve(String expression, Map<String, Object> variables) {
        AviatorContext aviatorContext = AviatorContext.create(expression, variables);
        if (StrUtil.isBlank(expression)) {
            return true;
        }
        return AviatorExecutor.executeBoolean(aviatorContext);
    }
}
