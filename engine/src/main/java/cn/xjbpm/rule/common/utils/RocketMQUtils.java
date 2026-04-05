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
package cn.xjbpm.rule.common.utils;

import cn.xjbpm.rule.engine.definition.model.nodes.RocketMQNode;
import cn.xjbpm.rule.engine.runtime.model.credentials.RocketMQCredential;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * RocketMQ工具类（消息生产者）
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Slf4j
public class RocketMQUtils {

    private RocketMQUtils() {
    }

    /**
     * 使用缓存的Producer发送消息到RocketMQ
     *
     * @param credential  RocketMQ凭据（包含缓存的Producer）
     * @param topic       主题
     * @param tag         标签
     * @param messageKey  消息键
     * @param messageBody 消息体
     * @param sendMode    发送模式
     * @param delayLevel  延迟级别
     * @param properties  消息属性JSON
     * @return 操作结果
     */
    public static RocketMQResult send(RocketMQCredential credential, String topic, String tag,
                                      String messageKey, String messageBody,
                                      RocketMQNode.SendMode sendMode,
                                      Integer delayLevel, String properties) {
        DefaultMQProducer producer = credential.getProducer();
        if (producer == null) {
            return RocketMQResult.builder()
                    .success(false)
                    .errorMessage("Producer未初始化")
                    .build();
        }

        try {
            Message message = createMessage(topic, tag, messageKey, messageBody, delayLevel, properties);

            switch (sendMode) {
                case SYNC:
                    return sendSync(producer, message);
                case ASYNC:
                    return sendAsync(producer, message, topic);
                case ONEWAY:
                    return sendOneway(producer, message, topic);
                default:
                    return sendSync(producer, message);
            }
        } catch (Exception e) {
            log.error("RocketMQ消息发送失败: {}", e.getMessage(), e);
            return RocketMQResult.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    /**
     * 创建消息
     */
    private static Message createMessage(String topic, String tag, String key,
                                         String body, Integer delayLevel, String properties) {
        Message message = new Message(topic, tag, key, body.getBytes(StandardCharsets.UTF_8));

        if (delayLevel != null && delayLevel > 0) {
            message.setDelayTimeLevel(delayLevel);
        }

        if (StringUtils.isNotBlank(properties)) {
            try {
                Map<String, String> propsMap = JsonUtils.json2Obj(properties, Map.class);
                if (propsMap != null) {
                    propsMap.forEach((k, v) -> {
                        if (v != null) {
                            message.putUserProperty(k, v);
                        }
                    });
                }
            } catch (Exception e) {
                log.warn("解析消息属性失败: {}", e.getMessage());
            }
        }

        return message;
    }

    /**
     * 同步发送
     */
    private static RocketMQResult sendSync(DefaultMQProducer producer, Message message) {
        try {
            SendResult sendResult = producer.send(message);
            log.info("RocketMQ消息发送成功: topic={}, msgId={}",
                    sendResult.getMessageQueue().getTopic(), sendResult.getMsgId());

            return RocketMQResult.builder()
                    .success(true)
                    .topic(sendResult.getMessageQueue().getTopic())
                    .brokerName(sendResult.getMessageQueue().getBrokerName())
                    .queueId(sendResult.getMessageQueue().getQueueId())
                    .msgId(sendResult.getMsgId())
                    .build();
        } catch (Exception e) {
            log.error("RocketMQ同步发送失败: {}", e.getMessage(), e);
            return RocketMQResult.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    /**
     * 异步发送
     */
    private static RocketMQResult sendAsync(DefaultMQProducer producer, Message message, String topic) {
        try {
            producer.send(message, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    log.info("RocketMQ异步发送成功: topic={}, msgId={}",
                            sendResult.getMessageQueue().getTopic(), sendResult.getMsgId());
                }

                @Override
                public void onException(Throwable e) {
                    log.error("RocketMQ异步发送失败: {}", e.getMessage(), e);
                }
            });

            return RocketMQResult.builder()
                    .success(true)
                    .topic(topic)
                    .build();
        } catch (Exception e) {
            log.error("RocketMQ异步发送失败: {}", e.getMessage(), e);
            return RocketMQResult.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    /**
     * 单向发送
     */
    private static RocketMQResult sendOneway(DefaultMQProducer producer, Message message, String topic) {
        try {
            producer.sendOneway(message);
            log.info("RocketMQ单向发送成功: topic={}", topic);

            return RocketMQResult.builder()
                    .success(true)
                    .topic(topic)
                    .build();
        } catch (Exception e) {
            log.error("RocketMQ单向发送失败: {}", e.getMessage(), e);
            return RocketMQResult.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    /**
     * RocketMQ操作结果
     */
    @lombok.Builder
    @lombok.Data
    public static class RocketMQResult {
        private boolean success;
        private String errorMessage;
        private String topic;
        private String brokerName;
        private int queueId;
        private String msgId;
    }
}
