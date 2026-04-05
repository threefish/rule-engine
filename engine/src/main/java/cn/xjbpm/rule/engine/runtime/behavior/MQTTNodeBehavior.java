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

import cn.xjbpm.rule.common.utils.MQTTUtils;
import cn.xjbpm.rule.engine.aviator.AviatorContext;
import cn.xjbpm.rule.engine.aviator.AviatorExecutor;
import cn.xjbpm.rule.engine.definition.model.nodes.MQTTNode;
import cn.xjbpm.rule.engine.runtime.model.FlowContext;
import cn.xjbpm.rule.engine.runtime.model.credentials.MQTTCredential;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * MQTT节点行为处理器（消息发布者）
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Slf4j
@SuppressWarnings("all")
public class MQTTNodeBehavior implements NodeBehavior {

    private final MQTTNode node;

    public MQTTNodeBehavior(MQTTNode node) {
        this.node = node;
    }

    @Override
    public void execution(FlowContext context) throws Exception {


        MQTTCredential mqttCredential = context.getEngineServices()
                .getCredentialsManager()
                .getMQTTCredential(node.getCredentialId());

        log.info("执行MQTT节点: {}", node.getName());

        MQTTExecutionContext execContext = resolveExpressions(context);
        MQTTUtils.MQTTResult result = executeMQTT(mqttCredential, execContext);
        Map<String, Object> resultMap = buildResultMap(result, execContext);
        context.setNodeOutput(node.getId(), resultMap);

        if (context.isDebugModel()) {
            context.addTraceLog(node.getId(), "MQTT消息发布操作完成: {}",
                    new Object[]{result.isSuccess() ? "成功" : "失败"});
        }
    }

    private MQTTExecutionContext resolveExpressions(FlowContext context) {
        Map<String, Object> variable = context.getVariable();
        return MQTTExecutionContext.builder()
                .topic(evaluateString(node.getTopic(), variable))
                .message(evaluateString(node.getMessage(), variable))
                .build();
    }

    private String evaluateString(String expression, Map<String, Object> variable) {
        if (expression == null) {
            return null;
        }
        return AviatorExecutor.evaluateString(AviatorContext.create(expression, variable));
    }


    private MQTTUtils.MQTTResult executeMQTT(MQTTCredential credential, MQTTExecutionContext execContext) {
        Assert.notNull(credential, "MQTT凭据不能为空");
        return MQTTUtils.publish(
                credential,
                execContext.topic,
                execContext.message,
                node.getQos(),
                node.isRetained()
        );
    }

    private Map<String, Object> buildResultMap(MQTTUtils.MQTTResult result, MQTTExecutionContext execContext) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("success", result.isSuccess());
        resultMap.put("errorMessage", result.getErrorMessage());
        resultMap.put("messageBody", execContext.getMessage());
        resultMap.put("topic", result.getTopic());
        return resultMap;
    }

    @lombok.Builder
    @lombok.Data
    private static class MQTTExecutionContext {
        private String topic;
        private String message;
    }
}
