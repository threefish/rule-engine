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

import cn.xjbpm.rule.common.utils.KafkaUtils;
import cn.xjbpm.rule.engine.aviator.AviatorContext;
import cn.xjbpm.rule.engine.aviator.AviatorExecutor;
import cn.xjbpm.rule.engine.definition.model.nodes.KafkaNode;
import cn.xjbpm.rule.engine.runtime.model.FlowContext;
import cn.xjbpm.rule.engine.runtime.model.credentials.KafkaCredential;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka节点行为处理器（消息生产者）
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Slf4j
@SuppressWarnings("all")
public class KafkaNodeBehavior implements NodeBehavior {

    private final KafkaNode node;

    public KafkaNodeBehavior(KafkaNode node) {
        this.node = node;
    }

    @Override
    public void execution(FlowContext context) throws Exception {

        KafkaCredential kafkaCredential = context.getBeanContextManager()
                .getCredentialsManager()
                .getKafkaCredential(node.getCredentialId());

        log.info("执行Kafka节点: {}", node.getName());

        KafkaExecutionContext execContext = resolveExpressions(context);
        KafkaUtils.KafkaResult result = executeKafka(kafkaCredential, execContext);
        Map<String, Object> resultMap = buildResultMap(result, execContext);
        context.put(node.getId(), resultMap);

        if (context.isDebugModel()) {
            context.addTraceLog(node.getId(), "Kafka消息生产操作完成: {}",
                    new Object[]{result.isSuccess() ? "成功" : "失败"});
        }
    }

    private KafkaExecutionContext resolveExpressions(FlowContext context) {
        Map<String, Object> variable = context.getVariable();
        return KafkaExecutionContext.builder()
                .topic(evaluateString(node.getTopic(), variable))
                .messageKey(evaluateString(node.getMessageKey(), variable))
                .messageValue(evaluateString(node.getMessageValue(), variable))
                .headers(evaluateString(node.getHeaders(), variable))
                .build();
    }

    private String evaluateString(String expression, Map<String, Object> variable) {
        if (expression == null) {
            return null;
        }
        return AviatorExecutor.evaluateString(AviatorContext.create(expression, variable));
    }


    private KafkaUtils.KafkaResult executeKafka(KafkaCredential credential, KafkaExecutionContext execContext) {
        Assert.notNull(credential, "Kafka凭据不能为空");
        return KafkaUtils.produce(
                credential,
                execContext.topic,
                execContext.messageKey,
                execContext.messageValue,
                node.getAckMode(),
                node.getPartition(),
                execContext.headers
        );
    }

    private Map<String, Object> buildResultMap(KafkaUtils.KafkaResult result, KafkaExecutionContext execContext) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("success", result.isSuccess());
        resultMap.put("errorMessage", result.getErrorMessage());
        resultMap.put("messageBody", execContext.getMessageValue());
        resultMap.put("topic", result.getTopic());
        resultMap.put("partition", result.getPartition());
        resultMap.put("offset", result.getOffset());
        return resultMap;
    }

    @lombok.Builder
    @lombok.Data
    private static class KafkaExecutionContext {
        private String topic;
        private String messageKey;
        private String messageValue;
        private String headers;
    }
}
