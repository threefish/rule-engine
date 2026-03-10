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
import cn.xjbpm.rule.common.utils.RabbitMQUtils;
import cn.xjbpm.rule.engine.aviator.AviatorContext;
import cn.xjbpm.rule.engine.aviator.AviatorExecutor;
import cn.xjbpm.rule.engine.definition.model.nodes.RabbitMQNode;
import cn.xjbpm.rule.engine.runtime.model.FlowContext;
import cn.xjbpm.rule.engine.runtime.model.credentials.RabbitMQCredential;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ节点行为处理器（消息生产者）
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Slf4j
@SuppressWarnings("all")
public class RabbitMQNodeBehavior implements NodeBehavior {

    private final RabbitMQNode node;

    public RabbitMQNodeBehavior(RabbitMQNode node) {
        this.node = node;
    }

    @Override
    public void execution(FlowContext context) throws Exception {
        if (RuleFlowConstant.DEMO_MODE) {
            handleDemoMode(context);
            return;
        }

        RabbitMQCredential rabbitMQCredential = context.getBeanContextManager()
                .getCredentialsManager()
                .getRabbitMQCredential(node.getCredentialId());

        log.info("执行RabbitMQ节点: {}", node.getName());

        RabbitMQExecutionContext execContext = resolveExpressions(context);
        RabbitMQUtils.RabbitMQResult result = executeRabbitMQ(rabbitMQCredential, execContext);
        Map<String, Object> resultMap = buildResultMap(result, execContext);
        context.put(node.getId(), resultMap);

        if (context.isDebugModel()) {
            context.addTraceLog(node.getId(), "RabbitMQ消息发布操作完成: {}",
                    new Object[]{result.isSuccess() ? "成功" : "失败"});
        }
    }

    private RabbitMQExecutionContext resolveExpressions(FlowContext context) {
        Map<String, Object> variable = context.getVariable();
        return RabbitMQExecutionContext.builder()
                .exchangeName(evaluateString(node.getExchangeName(), variable))
                .routingKey(evaluateString(node.getRoutingKey(), variable))
                .messageBody(evaluateString(node.getMessageBody(), variable))
                .properties(evaluateString(node.getConfigProperties(), variable))
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

    private RabbitMQUtils.RabbitMQResult executeRabbitMQ(RabbitMQCredential credential, RabbitMQExecutionContext execContext) {
        Assert.notNull(credential, "RabbitMQ凭据不能为空");
        return RabbitMQUtils.publish(
                credential,
                execContext.exchangeName,
                node.getExchangeType(),
                execContext.routingKey,
                execContext.messageBody,
                node.getDeliveryMode(),
                execContext.properties
        );
    }

    private Map<String, Object> buildResultMap(RabbitMQUtils.RabbitMQResult result, RabbitMQExecutionContext execContext) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("success", result.isSuccess());
        resultMap.put("messageBody", execContext.getMessageBody());
        resultMap.put("errorMessage", result.getErrorMessage());
        resultMap.put("exchangeName", result.getExchangeName());
        resultMap.put("routingKey", result.getRoutingKey());
        return resultMap;
    }

    @lombok.Builder
    @lombok.Data
    private static class RabbitMQExecutionContext {
        private String exchangeName;
        private String routingKey;
        private String messageBody;
        private String properties;
    }
}
