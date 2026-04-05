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

package cn.xjbpm.rule.engine.runtime.behavior;

import cn.xjbpm.rule.common.utils.QLExpressUtils;
import cn.xjbpm.rule.common.utils.groovy.GroovyShellUtil;
import cn.xjbpm.rule.engine.aviator.AviatorContext;
import cn.xjbpm.rule.engine.aviator.AviatorExecutor;
import cn.xjbpm.rule.engine.definition.model.nodes.FunctionNode;
import cn.xjbpm.rule.engine.runtime.model.FlowContext;
import lombok.extern.slf4j.Slf4j;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;

import java.util.HashMap;
import java.util.Map;

/**
 * 函数节点行为
 *
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/30
 */
@Slf4j
public class FunctionNodeBehavior implements NodeBehavior {

    public static final String RESULT = "result";

    private static final String VARIABLES_KEY = "variables";
    private static final String LOG_KEY = "log";

    private final FunctionNode node;

    public FunctionNodeBehavior(FunctionNode node) {
        this.node = node;
    }

    @Override
    public void execution(FlowContext context) {
        log.info("执行函数节点: [{}] type: {}", node.getName(), node.getScriptType());
        FunctionNode.ScriptType scriptType = node.getScriptType();
        Map<String, Object> resultMap = new HashMap<>(2);
        Object result;

        try {
            switch (node.getScriptType()) {
                case AVIATOR:
                    result = executeAviator(context);
                    break;
                case GROOVY:
                    result = executeGroovy(context);
                    break;
                case JAVASCRIPT:
                    result = executeJavaScript(context);
                    break;
                case QLExpress:
                    result = executeQLExpress(context);
                    break;
                default:
                    log.warn("未实现的脚本类型: {}", scriptType);
                    result = null;
            }
        } catch (Exception e) {
            log.error("节点 [{}] 脚本执行异常: {}", node.getName(), e.getMessage(), e);
            throw new RuntimeException("脚本执行失败: " + e.getMessage(), e);
        }

        resultMap.put(RESULT, result);
        context.setNodeOutput(node.getId(), resultMap);
    }

    /**
     * 执行 QLExpress 脚本
     */
    private Object executeQLExpress(FlowContext context) {
        log.debug("执行 QLExpress 脚本");
        Map<String, Object> env = new HashMap<>();
        env.put(VARIABLES_KEY, context.getVariable());
        return QLExpressUtils.execute(node.getScriptContent(), env);
    }

    /**
     * 执行 Aviator 脚本
     */
    private Object executeAviator(FlowContext context) {
        log.debug("执行 Aviator 脚本");
        Map<String, Object> env = new HashMap<>();
        env.put(VARIABLES_KEY, context.getVariable());
        AviatorContext aviatorContext = AviatorContext.builder()
                .cached(true)
                .expression(node.getScriptContent())
                .env(env)
                .build();
        return AviatorExecutor.execute(aviatorContext);
    }

    /**
     * 执行 Groovy 脚本
     */
    private Object executeGroovy(FlowContext context) {
        log.debug("执行 Groovy 脚本");
        Map<String, Object> env = new HashMap<>();
        env.put(VARIABLES_KEY, context.getVariable());
        return GroovyShellUtil.runScript(node.getScriptContent(), env);
    }

    /**
     * 执行 JavaScript 脚本 (GraalJS)
     */
    private Object executeJavaScript(FlowContext context) {
        log.debug("执行 JavaScript 脚本");
        String languageId = "js";
        try (Context jsContext = Context.newBuilder(languageId)
                .allowHostAccess(HostAccess.ALL)
                .allowHostClassLookup(className -> true)
                .build()) {
            Value bindings = jsContext.getBindings(languageId);
            bindings.putMember(VARIABLES_KEY, context.getVariable());
            bindings.putMember(LOG_KEY, log);
            return convertValue(jsContext.eval(languageId, node.getScriptContent()));
        }
    }

    /**
     * 转换 GraalJS Value 到 Java 对象
     */
    private Object convertValue(Value value) {
        if (value == null || value.isNull()) {
            return null;
        }
        if (value.isHostObject()) {
            return value.asHostObject();
        }
        if (value.isBoolean()) {
            return value.asBoolean();
        }
        if (value.isNumber()) {
            if (value.fitsInInt()) {
                return value.asInt();
            } else if (value.fitsInLong()) {
                return value.asLong();
            } else {
                return value.asDouble();
            }
        }
        if (value.isString()) {
            return value.asString();
        }
        if (value.hasArrayElements()) {
            int size = (int) value.getArraySize();
            Object[] array = new Object[size];
            for (int i = 0; i < size; i++) {
                array[i] = convertValue(value.getArrayElement(i));
            }
            return array;
        }
        if (value.hasMembers()) {
            Map<String, Object> map = new HashMap<>();
            for (String key : value.getMemberKeys()) {
                map.put(key, convertValue(value.getMember(key)));
            }
            return map;
        }
        return value.as(Object.class);
    }
}
