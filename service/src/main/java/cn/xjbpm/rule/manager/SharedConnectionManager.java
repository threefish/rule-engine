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

import cn.xjbpm.rule.node.enums.TriggerMode;
import com.dingtalk.open.app.api.OpenDingTalkClient;
import com.lark.oapi.ws.Client;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 连接共享管理器
 * 负责管理多个规则流之间的连接共享，实现连接复用和引用计数机制
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Component
@Slf4j
public class SharedConnectionManager {

    /**
     * 连接Key -> 共享连接对象
     */
    private final Map<String, SharedConnection> sharedConnections = new ConcurrentHashMap<>();

    /**
     * 规则流Key -> 连接Key
     */
    private final Map<String, String> flowConnectionMapping = new ConcurrentHashMap<>();

    /**
     * 根据触发类型和配置生成唯一连接标识
     *
     * @param type   触发类型
     * @param config 配置参数
     * @return 连接Key
     */
    public String generateConnectionKey(TriggerMode type, Map<String, String> config) {
        String credentialId = config.getOrDefault("credentialId", "");
        switch (type) {
            case RABBIT_MQ:
                String queueName = config.getOrDefault("queueName", "");
                return String.format("rabbitmq:%s:%s", credentialId, queueName);
            case KAFKA:
                String kafkaTopic = config.getOrDefault("topic", "");
                String groupId = config.getOrDefault("groupId", "");
                return String.format("kafka:%s:%s:%s", credentialId, kafkaTopic, groupId);
            case MQTT:
                String mqttTopic = config.getOrDefault("topic", "");
                return String.format("mqtt:%s:%s", credentialId, mqttTopic);
            case ROCKETMQ:
                String rocketTopic = config.getOrDefault("topic", "");
                String rocketGroupId = config.getOrDefault("groupId", "");
                return String.format("rocketmq:%s:%s:%s", credentialId, rocketTopic, rocketGroupId);
            case DINGTALK:
                return String.format("dingtalk:%s", credentialId);
            case FEISHU:
                return String.format("feishu:%s", credentialId);
            default:
                log.warn("不支持的触发类型: {}", type);
                return null;
        }
    }

    /**
     * 订阅连接
     * 当订阅者数量从0变为1时，返回true表示需要创建连接
     *
     * @param connectionKey 连接标识
     * @param ruleFlowKey   规则流标识
     * @param type          资源类型
     * @return 是否需要创建连接
     */
    public boolean subscribe(String connectionKey, String ruleFlowKey, ResourceCacheManager.ResourceType type) {
        if (connectionKey == null || ruleFlowKey == null) {
            log.warn("订阅连接失败: connectionKey或ruleFlowKey为空");
            return false;
        }

        boolean needCreateConnection = false;

        SharedConnection sharedConnection = sharedConnections.computeIfAbsent(connectionKey, key -> {
            SharedConnection conn = new SharedConnection();
            conn.setConnectionKey(key);
            conn.setType(type);
            conn.setCreateTime(System.currentTimeMillis());
            log.info("创建新的共享连接对象: connectionKey={}", key);
            return conn;
        });

        synchronized (sharedConnection) {
            int beforeSize = sharedConnection.getSubscribers().size();
            sharedConnection.getSubscribers().add(ruleFlowKey);
            flowConnectionMapping.put(ruleFlowKey, connectionKey);

            if (beforeSize == 0) {
                needCreateConnection = true;
                log.info("首个订阅者加入，需要创建连接: connectionKey={}, ruleFlowKey={}", connectionKey, ruleFlowKey);
            } else {
                log.info("订阅者加入，复用现有连接: connectionKey={}, ruleFlowKey={}, 当前订阅者数={}",
                        connectionKey, ruleFlowKey, sharedConnection.getSubscribers().size());
            }
        }

        return needCreateConnection;
    }

    /**
     * 取消订阅
     * 当订阅者数量从1变为0时，返回true表示需要关闭连接
     *
     * @param ruleFlowKey 规则流标识
     * @return 是否需要关闭连接
     */
    public boolean unsubscribe(String ruleFlowKey) {
        if (ruleFlowKey == null) {
            log.warn("取消订阅失败: ruleFlowKey为空");
            return false;
        }

        String connectionKey = flowConnectionMapping.remove(ruleFlowKey);
        if (connectionKey == null) {
            log.debug("未找到规则流对应的连接映射: ruleFlowKey={}", ruleFlowKey);
            return false;
        }

        SharedConnection sharedConnection = sharedConnections.get(connectionKey);
        if (sharedConnection == null) {
            log.warn("未找到共享连接对象: connectionKey={}", connectionKey);
            return false;
        }

        boolean needCloseConnection = false;

        synchronized (sharedConnection) {
            sharedConnection.getSubscribers().remove(ruleFlowKey);
            int remainingSubscribers = sharedConnection.getSubscribers().size();

            if (remainingSubscribers == 0) {
                needCloseConnection = true;
                sharedConnections.remove(connectionKey);
                log.info("最后一个订阅者离开，需要关闭连接: connectionKey={}, ruleFlowKey={}", connectionKey, ruleFlowKey);
            } else {
                log.info("订阅者离开，连接保持: connectionKey={}, ruleFlowKey={}, 剩余订阅者数={}",
                        connectionKey, ruleFlowKey, remainingSubscribers);
            }
        }

        return needCloseConnection;
    }

    /**
     * 获取共享连接对象
     *
     * @param connectionKey 连接标识
     * @return 共享连接对象，不存在则返回null
     */
    public SharedConnection getSharedConnection(String connectionKey) {
        return sharedConnections.get(connectionKey);
    }

    /**
     * 获取指定连接的所有订阅者
     *
     * @param connectionKey 连接标识
     * @return 订阅者集合
     */
    public Set<String> getSubscribers(String connectionKey) {
        SharedConnection sharedConnection = sharedConnections.get(connectionKey);
        if (sharedConnection == null) {
            return Set.of();
        }
        return Set.copyOf(sharedConnection.getSubscribers());
    }

    /**
     * 检查连接是否存在
     *
     * @param connectionKey 连接标识
     * @return 是否存在
     */
    public boolean hasConnection(String connectionKey) {
        return sharedConnections.containsKey(connectionKey);
    }

    /**
     * 根据规则流Key获取连接Key
     *
     * @param ruleFlowKey 规则流标识
     * @return 连接标识，不存在则返回null
     */
    public String getConnectionKeyByFlow(String ruleFlowKey) {
        return flowConnectionMapping.get(ruleFlowKey);
    }

    /**
     * 获取当前共享连接数量
     *
     * @return 共享连接数量
     */
    public int getConnectionCount() {
        return sharedConnections.size();
    }

    /**
     * 获取当前订阅映射数量
     *
     * @return 订阅映射数量
     */
    public int getMappingCount() {
        return flowConnectionMapping.size();
    }

    /**
     * 清理所有连接和映射
     */
    public void clear() {
        sharedConnections.clear();
        flowConnectionMapping.clear();
        log.info("已清理所有共享连接和映射");
    }

    /**
     * 清理所有连接和映射（别名方法）
     */
    public void clearAll() {
        clear();
    }

    /**
     * 获取所有连接Key
     *
     * @return 连接Key集合
     */
    public Set<String> getAllConnectionKeys() {
        return Set.copyOf(sharedConnections.keySet());
    }

    /**
     * 移除指定连接
     *
     * @param connectionKey 连接标识
     */
    public void removeConnection(String connectionKey) {
        if (connectionKey == null) {
            return;
        }
        sharedConnections.remove(connectionKey);
        log.info("移除共享连接: connectionKey={}", connectionKey);
    }

    /**
     * 设置RabbitMQ连接
     *
     * @param connectionKey 连接标识
     * @param connection    RabbitMQ连接
     * @param channel       RabbitMQ通道
     * @param consumerTag   消费者标签
     */
    public void setRabbitMQConnection(String connectionKey, Connection connection, Channel channel, String consumerTag) {
        SharedConnection sharedConnection = sharedConnections.get(connectionKey);
        if (sharedConnection != null) {
            sharedConnection.setRabbitMQConnection(connection);
            sharedConnection.setRabbitMQChannel(channel);
            sharedConnection.setRabbitMQConsumerTag(consumerTag);
            sharedConnection.setConnection(connection);
            log.info("设置RabbitMQ连接: connectionKey={}", connectionKey);
        }
    }

    /**
     * 设置Kafka连接
     *
     * @param connectionKey 连接标识
     * @param consumer      Kafka消费者
     */
    public void setKafkaConnection(String connectionKey, KafkaConsumer<String, String> consumer) {
        SharedConnection sharedConnection = sharedConnections.get(connectionKey);
        if (sharedConnection != null) {
            sharedConnection.setKafkaConsumer(consumer);
            sharedConnection.setConnection(consumer);
            log.info("设置Kafka连接: connectionKey={}", connectionKey);
        }
    }

    /**
     * 设置MQTT连接
     *
     * @param connectionKey 连接标识
     * @param mqttClient    MQTT客户端
     */
    public void setMQTTConnection(String connectionKey, MqttClient mqttClient) {
        SharedConnection sharedConnection = sharedConnections.get(connectionKey);
        if (sharedConnection != null) {
            sharedConnection.setMqttClient(mqttClient);
            sharedConnection.setConnection(mqttClient);
            log.info("设置MQTT连接: connectionKey={}", connectionKey);
        }
    }

    /**
     * 设置RocketMQ连接
     *
     * @param connectionKey 连接标识
     * @param consumer      RocketMQ消费者
     */
    public void setRocketMQConnection(String connectionKey, DefaultMQPushConsumer consumer) {
        SharedConnection sharedConnection = sharedConnections.get(connectionKey);
        if (sharedConnection != null) {
            sharedConnection.setRocketMQConsumer(consumer);
            sharedConnection.setConnection(consumer);
            log.info("设置RocketMQ连接: connectionKey={}", connectionKey);
        }
    }

    /**
     * 设置钉钉连接
     *
     * @param connectionKey 连接标识
     * @param client        钉钉客户端
     */
    public void setDingtalkConnection(String connectionKey, OpenDingTalkClient client) {
        SharedConnection sharedConnection = sharedConnections.get(connectionKey);
        if (sharedConnection != null) {
            sharedConnection.setDingtalkClient(client);
            sharedConnection.setConnection(client);
            log.info("设置钉钉连接: connectionKey={}", connectionKey);
        }
    }

    /**
     * 设置飞书连接
     *
     * @param connectionKey 连接标识
     * @param wsClient      飞书WebSocket客户端
     */
    public void setFeishuConnection(String connectionKey, Client wsClient) {
        SharedConnection sharedConnection = sharedConnections.get(connectionKey);
        if (sharedConnection != null) {
            sharedConnection.setFeishuClient(wsClient);
            sharedConnection.setConnection(wsClient);
            log.info("设置飞书连接: connectionKey={}", connectionKey);
        }
    }

    /**
     * 清理所有订阅（别名方法）
     */
    public void clearAllSubscriptions() {
        clear();
    }

    /**
     * 共享连接对象
     * 封装连接信息和订阅者列表
     */
    @Data
    public static class SharedConnection {
        /**
         * 连接标识
         */
        private String connectionKey;

        /**
         * 资源类型
         */
        private ResourceCacheManager.ResourceType type;

        /**
         * 通用连接对象引用
         */
        private Object connection;

        /**
         * 订阅的规则流Key集合
         */
        private Set<String> subscribers = ConcurrentHashMap.newKeySet();

        /**
         * 创建时间
         */
        private long createTime;

        /**
         * RabbitMQ连接
         */
        private Connection rabbitMQConnection;

        /**
         * RabbitMQ通道
         */
        private Channel rabbitMQChannel;

        /**
         * RabbitMQ消费者标签
         */
        private String rabbitMQConsumerTag;

        /**
         * Kafka消费者
         */
        private KafkaConsumer<String, String> kafkaConsumer;

        /**
         * MQTT客户端
         */
        private MqttClient mqttClient;

        /**
         * 钉钉客户端
         */
        private OpenDingTalkClient dingtalkClient;

        /**
         * 飞书客户端
         */
        private Client feishuClient;

        /**
         * RocketMQ消费者
         */
        private DefaultMQPushConsumer rocketMQConsumer;
    }
}
