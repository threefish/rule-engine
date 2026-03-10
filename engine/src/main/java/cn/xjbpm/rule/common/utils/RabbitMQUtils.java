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

import cn.xjbpm.rule.engine.definition.model.nodes.RabbitMQNode;
import cn.xjbpm.rule.engine.runtime.model.credentials.RabbitMQCredential;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * RabbitMQ 工具类（消息生产者）
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Slf4j
public class RabbitMQUtils {

    /**
     * 发布消息到RabbitMQ
     *
     * @param credential RabbitMQ凭据
     * @param exchangeName 交换机名称
     * @param exchangeType 交换机类型
     * @param routingKey 路由键
     * @param messageBody 消息体
     * @param deliveryMode 投递模式
     * @param properties 消息属性JSON
     * @return 操作结果
     */
    public static RabbitMQResult publish(RabbitMQCredential credential, String exchangeName,
                                          RabbitMQNode.ExchangeType exchangeType, String routingKey,
                                          String messageBody, RabbitMQNode.DeliveryMode deliveryMode,
                                          String properties) {
        ConnectionFactory factory = createConnectionFactory(credential);

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            if(StringUtils.isNotBlank(exchangeName)){
                declareExchange(channel, exchangeName, exchangeType);
            }

            AMQP.BasicProperties.Builder propsBuilder = new AMQP.BasicProperties.Builder();
            propsBuilder.deliveryMode(deliveryMode == RabbitMQNode.DeliveryMode.PERSISTENT ? 2 : 1);

            if (StringUtils.isNotBlank(properties)) {
                applyProperties(propsBuilder, properties);
            }

            String actualRoutingKey = exchangeType == RabbitMQNode.ExchangeType.FANOUT ? "" : routingKey;
            channel.basicPublish(exchangeName, actualRoutingKey, propsBuilder.build(),
                    messageBody.getBytes(StandardCharsets.UTF_8));

            log.info("RabbitMQ消息发布成功: exchange={}, routingKey={}", exchangeName, actualRoutingKey);

            return RabbitMQResult.builder()
                    .success(true)
                    .exchangeName(exchangeName)
                    .routingKey(actualRoutingKey)
                    .build();

        } catch (IOException | TimeoutException e) {
            log.error("RabbitMQ消息发布失败: {}", e.getMessage(), e);
            return RabbitMQResult.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    /**
     * 创建连接工厂
     */
    private static ConnectionFactory createConnectionFactory(RabbitMQCredential credential) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(credential.getHost());
        factory.setPort(credential.getPort());
        factory.setUsername(credential.getUsername());
        factory.setPassword(credential.getPassword());
        factory.setVirtualHost(credential.getVirtualHost());
        return factory;
    }

    /**
     * 声明交换机
     */
    private static void declareExchange(Channel channel, String exchangeName, RabbitMQNode.ExchangeType exchangeType)
            throws IOException {
        String type;
        switch (exchangeType) {
            case DIRECT:
                type = BuiltinExchangeType.DIRECT.getType();
                break;
            case TOPIC:
                type = BuiltinExchangeType.TOPIC.getType();
                break;
            case FANOUT:
                type = BuiltinExchangeType.FANOUT.getType();
                break;
            case HEADERS:
                type = BuiltinExchangeType.HEADERS.getType();
                break;
            default:
                type = BuiltinExchangeType.DIRECT.getType();
        }
        channel.exchangeDeclare(exchangeName, type, true);
    }

    /**
     * 应用消息属性
     */
    private static void applyProperties(AMQP.BasicProperties.Builder propsBuilder, String properties) {
        try {
            Map<String, Object> propsMap = cn.xjbpm.rule.common.utils.JsonUtils.json2Obj(properties, Map.class);
            if (propsMap != null) {
                if (propsMap.containsKey("contentType")) {
                    propsBuilder.contentType((String) propsMap.get("contentType"));
                }
                if (propsMap.containsKey("contentEncoding")) {
                    propsBuilder.contentEncoding((String) propsMap.get("contentEncoding"));
                }
                if (propsMap.containsKey("messageId")) {
                    propsBuilder.messageId((String) propsMap.get("messageId"));
                }
                if (propsMap.containsKey("correlationId")) {
                    propsBuilder.correlationId((String) propsMap.get("correlationId"));
                }
                if (propsMap.containsKey("replyTo")) {
                    propsBuilder.replyTo((String) propsMap.get("replyTo"));
                }
                if (propsMap.containsKey("expiration")) {
                    propsBuilder.expiration((String) propsMap.get("expiration"));
                }
                if (propsMap.containsKey("priority")) {
                    propsBuilder.priority(((Number) propsMap.get("priority")).intValue());
                }
            }
        } catch (Exception e) {
            log.warn("解析消息属性失败: {}", e.getMessage());
        }
    }

    /**
     * RabbitMQ操作结果
     */
    @lombok.Builder
    @lombok.Data
    public static class RabbitMQResult {
        private boolean success;
        private String errorMessage;
        private String exchangeName;
        private String routingKey;
    }
}
