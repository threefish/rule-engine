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

package cn.xjbpm.rule.engine.aviator;

import cn.hutool.core.util.StrUtil;
import cn.xjbpm.rule.common.utils.ClassScanner;
import cn.xjbpm.rule.common.utils.JsonUtils;
import cn.xjbpm.rule.engine.aviator.annotation.FunctionNamespace;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Options;
import com.googlecode.aviator.runtime.type.AviatorFunction;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Modifier;
import java.math.MathContext;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/6/30
 */
@Slf4j
public class AviatorExecutor {

    /**
     * 使用 DOTALL 模式允许匹配换行符，增加 trim() 容错
     */
    private static final Pattern DOUBLE_BRACE_PATTERN = Pattern.compile("\\{\\{(.+?)\\}\\}", Pattern.DOTALL);

    static {
        AviatorEvaluator.setOption(Options.ALWAYS_PARSE_FLOATING_POINT_NUMBER_INTO_DECIMAL, true);
        AviatorEvaluator.setOption(Options.ALWAYS_PARSE_INTEGRAL_NUMBER_INTO_DECIMAL, true);
        AviatorEvaluator.setOption(Options.MATH_CONTEXT, MathContext.DECIMAL128);
        AviatorEvaluator.addFunctionLoader(functionName -> {
            // 如果函数以 $ 开头，尝试加载不带 $ 的版本
            if (functionName.startsWith("$")) {
                String realName = functionName.substring(1);
                // 从引擎已注册的函数库中查找
                return AviatorEvaluator.getInstance().getFunction(realName);
            }
            return null;
        });
        ClassScanner classScanner = new ClassScanner(AviatorExecutor.class.getPackage().getName());
        Set<Class<?>> scans = classScanner.scan();
        scans.stream().filter(clazz -> AviatorFunction.class.isAssignableFrom(clazz)).filter(clazz -> !Modifier.isAbstract(clazz.getModifiers())).filter(clazz -> !Modifier.isInterface(clazz.getModifiers())).forEach(clazz -> {
            try {
                AviatorFunction aviatorFunction = (AviatorFunction) clazz.getDeclaredConstructor().newInstance();
                AviatorEvaluator.addFunction(aviatorFunction);
            } catch (Exception e) {
                log.error("Error loading custom function", e);
            }
        });
        List<Class<?>> aviatorFunctionClasses = scans.stream().filter(clazz -> clazz.isAnnotationPresent(FunctionNamespace.class)).collect(Collectors.toList());
        for (Class<?> clazz : aviatorFunctionClasses) {
            try {
                FunctionNamespace aviatorFunction = clazz.getAnnotation(FunctionNamespace.class);
                AviatorEvaluator.addStaticFunctions(aviatorFunction.name(), clazz);
            } catch (Exception e) {
                log.error("Error loading custom function", e);
            }
        }

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
        if (context.getEnv() instanceof SmartEnvMap == false) {
            log.warn("env 应该为 SmartEnvMap 请检查  表达式:{}", context.getExpression());
        }
        Object result = null;
        try {
            result = AviatorEvaluator.execute(context.getExpression(), context.getEnv(), context.isCached());
        } finally {
            if (log.isInfoEnabled()) {
                log.info("表达式执行器  表达式:{} 计算结果:{} 上下文:{}", context.getExpression(), result, JsonUtils.obj2Json(context));
            }
        }
        return result;
    }

    public static void main(String[] args) {
        Map<String, Object> env = JsonUtils.json2Obj("{\"用户\":{\"年龄\":38},\"list\":[{\"a\":1}]}", Map.class);
        // 正确的表达式示例
        String[] expressions = {"{{$list[0]['a']}}",              // 圆括号语法
        };
        for (String expr : expressions) {
            try {
                Object result = AviatorEvaluator.execute(expr, new SmartEnvMap(env));
                System.out.println(expr + " = " + result);
            } catch (Exception e) {
                System.err.println(expr + " 执行失败: " + e.getMessage());
            }
        }

    }

    /**
     * example: year: {{$NOW('yyyy-MM-dd HH:mm:ss')}} {{$EQUALS($用户.年龄,1)}} {{$EQUALS($NOW('yyyy-MM-dd HH:mm:ss'),1)}} {{$用户.年龄}}
     *
     * @param context
     * @return
     */
    public static String evaluateString(AviatorContext context) {
        String expression = context.getExpression();
        if (StrUtil.isBlank(expression)) {
            return expression;
        }

        // 场景 A: 如果不包含 {{ }}，说明可能只是普通字符串 直接返回原值
        if (!expression.contains("{{")) {
            return expression;
        }

        // 场景 B: 包含 {{ }}，走占位符替换逻辑
        StringBuilder sb = new StringBuilder();
        Matcher matcher = DOUBLE_BRACE_PATTERN.matcher(expression);
        int lastEnd = 0;
        while (matcher.find()) {
            // 添加匹配项之前的普通文本
            sb.append(expression, lastEnd, matcher.start());
            String content = matcher.group(1).trim();
            try {
                Object result = AviatorEvaluator.execute(content, context.getEnv(), context.isCached());
                sb.append(result == null ? "" : result);
            } catch (Exception e) {
                log.error("Execution failed for placeholder: {}", content, e);
                // 占位符内执行失败，保留原占位符文本或抛出异常
                sb.append(matcher.group(0));
            }
            lastEnd = matcher.end();
        }
        sb.append(expression.substring(lastEnd));
        return sb.toString();
    }
}