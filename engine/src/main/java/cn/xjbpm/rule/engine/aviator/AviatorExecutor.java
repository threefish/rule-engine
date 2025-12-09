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
package cn.xjbpm.rule.engine.aviator;

import cn.xjbpm.rule.common.utils.ClassScanner;
import cn.xjbpm.rule.common.utils.JsonUtils;
import cn.xjbpm.rule.engine.aviator.function.AviatorExtendFunction;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Options;
import com.googlecode.aviator.runtime.type.AviatorFunction;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Modifier;
import java.math.MathContext;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/6/30
 */
@Slf4j
public class AviatorExecutor {


    private static final Pattern FUNCTION_PATTERN = Pattern.compile("\\$\\{(.+?)\\}");

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
     * 执行结果，返回boolean类型
     *
     * @param context 上下文对象
     * @return
     */
    public static boolean executeBoolean(AviatorContext context) {
        return (Boolean) execute(context);
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
            log.debug("表达式执行器 计算结果:{} 表达式:{} 上下文:{}", result, context.getExpression(), JsonUtils.obj2Json(context));
        }
        return result;
    }

    public static void main(String[] args) {
        String expression = "year: ${NOW('yyyy-MM-dd HH:mm:ss')}";
        System.out.println("----------");
        System.out.println(evaluateAndReplace(AviatorContext.create(expression, null)));
        System.out.println("----------");
    }

    public static String evaluateAndReplace(AviatorContext context) {
        StringBuffer sb = new StringBuffer();
        Matcher matcher = FUNCTION_PATTERN.matcher(context.getExpression());
        while (matcher.find()) {
            String expressionContent = matcher.group(1);
            String resultValue;
            try {
                Object result = AviatorEvaluator.compile(expressionContent, true).execute(context.getEnv());
                resultValue = JsonUtils.escapeJsonString(result.toString());
            } catch (Exception e) {
                throw new RuntimeException(String.format("表达式:%s %s", expressionContent, e.getMessage()));
            }
            matcher.appendReplacement(sb, resultValue);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}