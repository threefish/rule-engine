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

import cn.xjbpm.rule.common.utils.RocketMQUtils;
import cn.xjbpm.rule.engine.aviator.AviatorContext;
import cn.xjbpm.rule.engine.aviator.AviatorExecutor;
import cn.xjbpm.rule.engine.definition.model.nodes.RocketMQNode;
import cn.xjbpm.rule.engine.runtime.model.FlowContext;
import cn.xjbpm.rule.engine.runtime.model.credentials.RocketMQCredential;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * RocketMQ节点行为处理器（消息生产者）
 * 使用缓存的Producer实例发送消息
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Slf4j
public class RocketMQNodeBehavior implements NodeBehavior {

    private final RocketMQNode node;

    public RocketMQNodeBehavior(RocketMQNode node) {
        this.node = node;
    }

    @Override
    public void execution(FlowContext context) throws Exception {

        RocketMQCredential rocketMQCredential = context.getEngineServices()
                .getCredentialsManager()
                .getRocketMQCredential(node.getCredentialId());

        log.info("执行RocketMQ节点: {}", node.getName());

        RocketMQExecutionContext execContext = resolveExpressions(context);
        RocketMQUtils.RocketMQResult result = executeRocketMQ(rocketMQCredential, execContext);
        Map<String, Object> resultMap = buildResultMap(result, execContext);
        context.setNodeOutput(node.getId(), resultMap);

        if (context.isDebugModel()) {
            context.addTraceLog(node.getId(), "RocketMQ消息发送操作完成: {}",
                    new Object[]{result.isSuccess() ? "成功" : "失败"});
        }
    }

    private RocketMQExecutionContext resolveExpressions(FlowContext context) {
        Map<String, Object> variable = context.getVariable();
        return RocketMQExecutionContext.builder()
                .topic(evaluateString(node.getTopic(), variable))
                .tag(evaluateString(node.getTag(), variable))
                .messageKey(evaluateString(node.getMessageKey(), variable))
                .messageBody(evaluateString(node.getMessageBody(), variable))
                .properties(evaluateString(node.getOtherProperties(), variable))
                .build();
    }

    private String evaluateString(String expression, Map<String, Object> variable) {
        if (expression == null) {
            return null;
        }
        return AviatorExecutor.evaluateString(AviatorContext.create(expression, variable));
    }

    private RocketMQUtils.RocketMQResult executeRocketMQ(RocketMQCredential credential, RocketMQExecutionContext execContext) {
        Assert.notNull(credential, "RocketMQ凭据不能为空");
        return RocketMQUtils.send(
                credential,
                execContext.topic,
                execContext.tag,
                execContext.messageKey,
                execContext.messageBody,
                node.getSendMode(),
                node.getDelayLevel(),
                execContext.properties
        );
    }

    private Map<String, Object> buildResultMap(RocketMQUtils.RocketMQResult result, RocketMQExecutionContext execContext) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("success", result.isSuccess());
        resultMap.put("errorMessage", result.getErrorMessage());
        resultMap.put("messageBody", execContext.getMessageBody());
        resultMap.put("topic", result.getTopic());
        resultMap.put("msgId", result.getMsgId());
        resultMap.put("brokerName", result.getBrokerName());
        resultMap.put("queueId", result.getQueueId());
        return resultMap;
    }

    @lombok.Builder
    @lombok.Data
    private static class RocketMQExecutionContext {
        private String topic;
        private String tag;
        private String messageKey;
        private String messageBody;
        private String properties;
    }
}
