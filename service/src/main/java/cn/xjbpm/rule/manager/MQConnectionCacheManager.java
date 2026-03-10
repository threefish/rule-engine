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

package cn.xjbpm.rule.manager;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MQ连接缓存管理器
 * 负责管理所有MQ连接的缓存、断开和刷新
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Component
@Slf4j
public class MQConnectionCacheManager {

    /**
     * RabbitMQ连接缓存
     * key: ruleFlowKey
     * value: MQConnectionWrapper
     */
    private final Map<String, MQConnectionWrapper> connectionCache = new ConcurrentHashMap<>();

    /**
     * 添加RabbitMQ连接到缓存
     *
     * @param ruleFlowKey 规则流唯一标识
     * @param connection RabbitMQ连接
     * @param channel RabbitMQ通道
     * @param consumerTag 消费者标签
     */
    public void addRabbitMQConnection(String ruleFlowKey, Connection connection, Channel channel, String consumerTag) {
        MQConnectionWrapper wrapper = new MQConnectionWrapper();
        wrapper.setType(MQType.RABBITMQ);
        wrapper.setRabbitMQConnection(connection);
        wrapper.setRabbitMQChannel(channel);
        wrapper.setRabbitMQConsumerTag(consumerTag);
        wrapper.setCreateTime(System.currentTimeMillis());
        connectionCache.put(ruleFlowKey, wrapper);
        log.info("RabbitMQ连接已缓存: ruleFlowKey={}", ruleFlowKey);
    }

    /**
     * 添加Kafka消费者到缓存
     *
     * @param ruleFlowKey 规则流唯一标识
     * @param consumer Kafka消费者
     */
    public void addKafkaConnection(String ruleFlowKey, KafkaConsumer<String, String> consumer) {
        MQConnectionWrapper wrapper = new MQConnectionWrapper();
        wrapper.setType(MQType.KAFKA);
        wrapper.setKafkaConsumer(consumer);
        wrapper.setCreateTime(System.currentTimeMillis());
        connectionCache.put(ruleFlowKey, wrapper);
        log.info("Kafka连接已缓存: ruleFlowKey={}", ruleFlowKey);
    }

    /**
     * 添加MQTT客户端到缓存
     *
     * @param ruleFlowKey 规则流唯一标识
     * @param mqttClient MQTT客户端
     */
    public void addMQTTConnection(String ruleFlowKey, MqttClient mqttClient) {
        MQConnectionWrapper wrapper = new MQConnectionWrapper();
        wrapper.setType(MQType.MQTT);
        wrapper.setMqttClient(mqttClient);
        wrapper.setCreateTime(System.currentTimeMillis());
        connectionCache.put(ruleFlowKey, wrapper);
        log.info("MQTT连接已缓存: ruleFlowKey={}", ruleFlowKey);
    }

    /**
     * 获取连接包装器
     *
     * @param ruleFlowKey 规则流唯一标识
     * @return 连接包装器
     */
    public MQConnectionWrapper getConnection(String ruleFlowKey) {
        return connectionCache.get(ruleFlowKey);
    }

    /**
     * 检查连接是否存在
     *
     * @param ruleFlowKey 规则流唯一标识
     * @return 是否存在
     */
    public boolean hasConnection(String ruleFlowKey) {
        return connectionCache.containsKey(ruleFlowKey);
    }

    /**
     * 断开并移除指定规则流的MQ连接
     *
     * @param ruleFlowKey 规则流唯一标识
     */
    public void disconnect(String ruleFlowKey) {
        MQConnectionWrapper wrapper = connectionCache.remove(ruleFlowKey);
        if (wrapper == null) {
            log.debug("未找到需要断开的MQ连接: ruleFlowKey={}", ruleFlowKey);
            return;
        }

        try {
            switch (wrapper.getType()) {
                case RABBITMQ:
                    disconnectRabbitMQ(wrapper, ruleFlowKey);
                    break;
                case KAFKA:
                    disconnectKafka(wrapper, ruleFlowKey);
                    break;
                case MQTT:
                    disconnectMQTT(wrapper, ruleFlowKey);
                    break;
            }
        } catch (Exception e) {
            log.error("断开MQ连接异常: ruleFlowKey={}, error={}", ruleFlowKey, e.getMessage(), e);
        }
    }

    /**
     * 断开RabbitMQ连接
     */
    private void disconnectRabbitMQ(MQConnectionWrapper wrapper, String ruleFlowKey) throws IOException {
        Channel channel = wrapper.getRabbitMQChannel();
        Connection connection = wrapper.getRabbitMQConnection();

        if (channel != null && channel.isOpen()) {
            try {
                String consumerTag = wrapper.getRabbitMQConsumerTag();
                if (consumerTag != null) {
                    channel.basicCancel(consumerTag);
                    log.info("RabbitMQ消费者已取消: ruleFlowKey={}, consumerTag={}", ruleFlowKey, consumerTag);
                }
                channel.close();
            } catch (Exception e) {
                log.warn("关闭RabbitMQ通道异常: {}", e.getMessage());
            }
        }

        if (connection != null && connection.isOpen()) {
            try {
                connection.close();
                log.info("RabbitMQ连接已断开: ruleFlowKey={}", ruleFlowKey);
            } catch (Exception e) {
                log.warn("关闭RabbitMQ连接异常: {}", e.getMessage());
            }
        }
    }

    /**
     * 断开Kafka连接
     */
    private void disconnectKafka(MQConnectionWrapper wrapper, String ruleFlowKey) {
        KafkaConsumer<String, String> consumer = wrapper.getKafkaConsumer();
        if (consumer != null) {
            try {
                consumer.wakeup();
                consumer.close();
                log.info("Kafka消费者已关闭: ruleFlowKey={}", ruleFlowKey);
            } catch (Exception e) {
                log.warn("关闭Kafka消费者异常: {}", e.getMessage());
            }
        }
    }

    /**
     * 断开MQTT连接
     */
    private void disconnectMQTT(MQConnectionWrapper wrapper, String ruleFlowKey) {
        MqttClient mqttClient = wrapper.getMqttClient();
        if (mqttClient != null) {
            try {
                if (mqttClient.isConnected()) {
                    mqttClient.disconnect();
                }
                mqttClient.close();
                log.info("MQTT客户端已断开: ruleFlowKey={}", ruleFlowKey);
            } catch (Exception e) {
                log.warn("关闭MQTT客户端异常: {}", e.getMessage());
            }
        }
    }

    /**
     * 断开所有MQ连接
     */
    public void disconnectAll() {
        log.info("开始断开所有MQ连接, 当前连接数: {}", connectionCache.size());
        connectionCache.keySet().forEach(this::disconnect);
        log.info("所有MQ连接已断开");
    }

    /**
     * 刷新指定规则流的MQ连接
     * 先断开旧连接，再建立新连接
     *
     * @param ruleFlowKey 规则流唯一标识
     */
    public void refresh(String ruleFlowKey) {
        log.info("刷新MQ连接: ruleFlowKey={}", ruleFlowKey);
        disconnect(ruleFlowKey);
    }

    /**
     * 获取所有缓存的连接key
     *
     * @return 连接key集合
     */
    public java.util.Set<String> getAllConnectionKeys() {
        return connectionCache.keySet();
    }

    /**
     * 获取当前连接数量
     *
     * @return 连接数量
     */
    public int getConnectionCount() {
        return connectionCache.size();
    }

    /**
     * MQ类型枚举
     */
    public enum MQType {
        RABBITMQ,
        KAFKA,
        MQTT
    }

    /**
     * MQ连接包装器
     */
    @lombok.Data
    public static class MQConnectionWrapper {
        private MQType type;
        private long createTime;

        private Connection rabbitMQConnection;
        private Channel rabbitMQChannel;
        private String rabbitMQConsumerTag;
        private DeliverCallback deliverCallback;

        private KafkaConsumer<String, String> kafkaConsumer;

        private MqttClient mqttClient;
    }
}
