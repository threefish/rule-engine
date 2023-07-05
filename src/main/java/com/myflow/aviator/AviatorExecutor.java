package com.myflow.aviator;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Options;
import com.myflow.aviator.function.incometax.GET_FAST_DEDUCTION;
import com.myflow.aviator.function.incometax.GET_INCOME_TAX_RATE;
import com.myflow.common.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;

import java.math.MathContext;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/6/30
 */
@Slf4j
public class AviatorExecutor {

    static {
        AviatorEvaluator.setOption(Options.ALWAYS_PARSE_FLOATING_POINT_NUMBER_INTO_DECIMAL, true);
        AviatorEvaluator.setOption(Options.ALWAYS_PARSE_INTEGRAL_NUMBER_INTO_DECIMAL, true);
        AviatorEvaluator.setOption(Options.MATH_CONTEXT, MathContext.DECIMAL128);
        AviatorEvaluator.addFunction(new GET_INCOME_TAX_RATE());
        AviatorEvaluator.addFunction(new GET_FAST_DEDUCTION());
    }

    private AviatorExecutor() {
    }

    /**
     * 执行结果
     *
     * @param context 上下文对象
     * @return
     */
    public static Object execute(AviatorContext context) {
        Object result = AviatorEvaluator.execute(context.getExpression(), context.getEnv(), context.isCached());
        if (log.isDebugEnabled()) {
            log.debug("Aviator执行器result={},context={}", result, JsonUtil.obj2Json(context));
        }
        return result;
    }

    /**
     * 执行结果，返回boolean类型
     *
     * @param context 上下文对象
     * @return
     */
    public static boolean executeBoolean(AviatorContext context) {
        return (Boolean) execute(context);
    }
}
