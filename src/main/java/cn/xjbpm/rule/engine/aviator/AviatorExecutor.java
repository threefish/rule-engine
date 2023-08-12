package cn.xjbpm.rule.engine.aviator;

import cn.hutool.core.lang.ClassScanner;
import cn.xjbpm.rule.engine.aviator.function.AviatorExtendFunction;
import cn.xjbpm.rule.engine.common.utils.JsonUtils;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Options;
import com.googlecode.aviator.runtime.type.AviatorFunction;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Modifier;
import java.math.MathContext;
import java.util.Set;

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
        ClassScanner classScanner = new ClassScanner(AviatorExtendFunction.class.getPackage().getName());
        Set<Class<?>> scans = classScanner.scan();
        scans.stream()
                .filter(clazz -> AviatorFunction.class.isAssignableFrom(clazz))
                .filter(clazz -> !Modifier.isAbstract(clazz.getModifiers()))
                .filter(clazz -> !Modifier.isInterface(clazz.getModifiers()))
                .forEach(clazz -> {
                    try {
                        AviatorFunction aviatorFunction = (AviatorFunction) clazz.getDeclaredConstructor().newInstance();
                        AviatorEvaluator.addFunction(aviatorFunction);
                    } catch (Exception e) {
                        log.error("Error loading custom function", e);
                    }
                });

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
        if (log.isInfoEnabled()) {
            log.info("表达式执行器 result:{} 表达式:{}", result, context.getExpression());
        } else if (log.isDebugEnabled()) {
            log.debug("表达式执行器 result:{} 表达式:{} 上下文:{}", result, context.getExpression(), JsonUtils.obj2Json(context));
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
