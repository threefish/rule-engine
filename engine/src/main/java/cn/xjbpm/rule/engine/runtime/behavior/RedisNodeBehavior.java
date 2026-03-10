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

import cn.xjbpm.rule.common.constant.RuleFlowConstant;
import cn.xjbpm.rule.common.utils.RedisUtils;
import cn.xjbpm.rule.engine.aviator.AviatorContext;
import cn.xjbpm.rule.engine.aviator.AviatorExecutor;
import cn.xjbpm.rule.engine.definition.model.nodes.RedisNode;
import cn.xjbpm.rule.engine.runtime.model.FlowContext;
import cn.xjbpm.rule.engine.runtime.model.credentials.RedisCredential;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * Redis节点行为处理器
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Slf4j
@SuppressWarnings("all")
public class RedisNodeBehavior implements NodeBehavior {

    private final RedisNode node;

    public RedisNodeBehavior(RedisNode node) {
        this.node = node;
    }

    @Override
    public void execution(FlowContext context) throws Exception {
        if (RuleFlowConstant.DEMO_MODE) {
            handleDemoMode(context);
            return;
        }

        RedisCredential redisCredential = context.getBeanContextManager()
                .getCredentialsManager()
                .getRedisCredential(node.getCredentialId());

        log.info("执行Redis节点: {}", node.getName());

        RedisExecutionContext execContext = resolveExpressions(context);
        RedisUtils.RedisResult result = executeRedis(redisCredential, execContext);
        Map<String, Object> resultMap = buildResultMap(result, execContext);
        context.put(node.getId(), resultMap);
        if (context.isDebugModel()) {
            context.addTraceLog(node.getId(), "Redis {} 操作完成: {}",
                    new Object[]{node.getOperationType().name(), result.isSuccess() ? "成功" : "失败"});
        }
    }

    private RedisExecutionContext resolveExpressions(FlowContext context) {
        Map<String, Object> variable = context.getVariable();
        return RedisExecutionContext.builder()
                .key(evaluateString(node.getKey(), variable))
                .field(evaluateString(node.getField(), variable))
                .value(evaluateString(node.getValue(), variable))
                .luaScript(evaluateString(node.getLuaScript(), variable))
                .keys(evaluateString(node.getKeys(), variable))
                .args(evaluateString(node.getArgs(), variable))
                .build();
    }

    private String evaluateString(String expression, Map<String, Object> variable) {
        if (expression == null) {
            return null;
        }
        return AviatorExecutor.evaluateString(AviatorContext.create(expression, variable));
    }

    private void handleDemoMode(FlowContext context) {
        context.addTraceLog(node.getId(), "演示模式不允许执行,已跳过");
        Map<String, Object> data = new HashMap<>();
        data.put("data", "演示模式不允许执行");
        data.put("errorCode", 0);
        context.put(node.getId(), data);
    }

    private RedisUtils.RedisResult executeRedis(RedisCredential credential, RedisExecutionContext execContext) {
        Assert.notNull(credential, "Redis凭据不能为空");
        return RedisUtils.execute(
                credential,
                node.getOperationType(),
                execContext.key,
                execContext.field,
                execContext.value,
                node.getTtl(),
                node.getStart(),
                node.getStop(),
                execContext.luaScript,
                node.getKeyCount(),
                execContext.keys,
                execContext.args
        );
    }

    private Map<String, Object> buildResultMap(RedisUtils.RedisResult result, RedisExecutionContext execContext) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("success", result.isSuccess());
        resultMap.put("data", result.getData());
        resultMap.put("errorMessage", result.getErrorMessage());
        resultMap.put("operationType", result.getOperationType());
        if (execContext.key != null) {
            resultMap.put("key", execContext.key);
        }
        return resultMap;
    }

    @lombok.Builder
    @lombok.Data
    private static class RedisExecutionContext {
        private String key;
        private String field;
        private String value;
        private String luaScript;
        private String keys;
        private String args;
    }
}
