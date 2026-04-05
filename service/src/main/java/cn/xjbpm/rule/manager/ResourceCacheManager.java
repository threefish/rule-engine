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

import cn.xjbpm.rule.dispatcher.TriggerSubscriptionManager;
import cn.xjbpm.rule.utils.ReflectUtil;
import com.dingtalk.open.app.api.OpenDingTalkClient;
import com.lark.oapi.okhttp.OkHttpClient;
import com.lark.oapi.ws.Client;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

/**
 * 负责管理所有缓存资源的连接、断开和刷新
 * 集成 SharedConnectionManager 实现连接共享和引用计数机制
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ResourceCacheManager {

    private final SharedConnectionManager sharedConnectionManager;
    private final TriggerSubscriptionManager triggerSubscriptionManager;

    /**
     * 检查连接是否存在
     *
     * @param ruleFlowKey 规则流唯一标识
     * @return 是否存在
     */
    public boolean hasConnection(String ruleFlowKey) {
        return sharedConnectionManager.getConnectionKeyByFlow(ruleFlowKey) != null;
    }

    /**
     * 断开并移除指定规则流的资源连接
     * 使用引用计数机制，只有当最后一个订阅者离开时才真正关闭连接
     *
     * @param ruleFlowKey 规则流唯一标识
     */
    public void disconnect(String ruleFlowKey) {
        String connectionKey = sharedConnectionManager.getConnectionKeyByFlow(ruleFlowKey);
        if (connectionKey == null) {
            log.debug("未找到需要断开的资源连接: ruleFlowKey={}", ruleFlowKey);
            return;
        }

        triggerSubscriptionManager.removeSubscription(connectionKey, ruleFlowKey);

        boolean needCloseConnection = sharedConnectionManager.unsubscribe(ruleFlowKey);

        if (needCloseConnection) {
            SharedConnectionManager.SharedConnection sharedConnection = sharedConnectionManager.getSharedConnection(connectionKey);
            if (sharedConnection != null) {
                closeConnection(sharedConnection);
                sharedConnectionManager.removeConnection(connectionKey);
            }
        }
    }

    /**
     * 根据连接类型关闭连接
     *
     * @param wrapper 共享连接对象
     */
    private void closeConnection(SharedConnectionManager.SharedConnection wrapper) {
        try {
            switch (wrapper.getType()) {
                case RABBITMQ:
                    closeRabbitMQConnection(wrapper);
                    break;
                case KAFKA:
                    closeKafkaConnection(wrapper);
                    break;
                case MQTT:
                    closeMQTTConnection(wrapper);
                    break;
                case DINGTALK:
                    closeDingtalkConnection(wrapper);
                    break;
                case FEISHU:
                    closeFeishuConnection(wrapper);
                    break;
                case ROCKETMQ:
                    closeRocketMQConnection(wrapper);
                    break;
            }
        } catch (Exception e) {
            log.error("关闭连接异常: {}", e.getMessage(), e);
        }
    }

    /**
     * 关闭RabbitMQ连接
     *
     * @param wrapper 共享连接对象
     */
    private void closeRabbitMQConnection(SharedConnectionManager.SharedConnection wrapper) throws IOException {
        Channel channel = wrapper.getRabbitMQChannel();
        Connection connection = wrapper.getRabbitMQConnection();

        if (channel != null && channel.isOpen()) {
            try {
                String consumerTag = wrapper.getRabbitMQConsumerTag();
                if (consumerTag != null) {
                    channel.basicCancel(consumerTag);
                    log.info("RabbitMQ消费者已取消: connectionKey={}, consumerTag={}", wrapper.getConnectionKey(), consumerTag);
                }
                channel.close();
            } catch (Exception e) {
                log.warn("关闭RabbitMQ通道异常: {}", e.getMessage());
            }
        }

        if (connection != null && connection.isOpen()) {
            try {
                connection.close();
                log.info("RabbitMQ连接已断开: connectionKey={}", wrapper.getConnectionKey());
            } catch (Exception e) {
                log.warn("关闭RabbitMQ连接异常: {}", e.getMessage());
            }
        }
    }

    /**
     * 关闭Kafka连接
     *
     * @param wrapper 共享连接对象
     */
    private void closeKafkaConnection(SharedConnectionManager.SharedConnection wrapper) {
        KafkaConsumer<String, String> consumer = wrapper.getKafkaConsumer();
        if (consumer != null) {
            try {
                consumer.wakeup();
                consumer.close();
                log.info("Kafka消费者已关闭: connectionKey={}", wrapper.getConnectionKey());
            } catch (Exception e) {
                log.warn("关闭Kafka消费者异常: {}", e.getMessage());
            }
        }
    }

    /**
     * 关闭MQTT连接
     *
     * @param wrapper 共享连接对象
     */
    private void closeMQTTConnection(SharedConnectionManager.SharedConnection wrapper) {
        MqttClient mqttClient = wrapper.getMqttClient();
        if (mqttClient != null) {
            try {
                if (mqttClient.isConnected()) {
                    mqttClient.disconnect();
                }
                mqttClient.close();
                log.info("MQTT客户端已断开: connectionKey={}", wrapper.getConnectionKey());
            } catch (Exception e) {
                log.warn("关闭MQTT客户端异常: {}", e.getMessage());
            }
        }
    }

    /**
     * 关闭钉钉连接
     *
     * @param wrapper 共享连接对象
     */
    private void closeDingtalkConnection(SharedConnectionManager.SharedConnection wrapper) {
        OpenDingTalkClient dingtalkClient = wrapper.getDingtalkClient();
        if (dingtalkClient != null) {
            try {
                dingtalkClient.stop();
                log.info("钉钉客户端已停止: connectionKey={}", wrapper.getConnectionKey());
            } catch (Exception e) {
                log.warn("关闭钉钉客户端异常: {}", e.getMessage());
            }
        }
    }

    /**
     * 关闭飞书连接
     *
     * @param wrapper 共享连接对象
     */
    private void closeFeishuConnection(SharedConnectionManager.SharedConnection wrapper) {
        Client feishuClient = wrapper.getFeishuClient();
        if (feishuClient != null) {
            try {
                log.info("开始停止飞书客户端实例: connectionKey={}", wrapper.getConnectionKey());
                ReflectUtil.reflectSetField(feishuClient, "autoReconnect", false);
                ExecutorService executor = (ExecutorService) ReflectUtil.reflectGetField(feishuClient, "executor");
                if (executor != null) {
                    executor.shutdownNow();
                    log.debug("已强制关闭 Client 线程池: connectionKey={}", wrapper.getConnectionKey());
                }
                ReflectUtil.reflectInvokeMethod(feishuClient, "disconnect");
                log.info("已通过反射执行 disconnect() 方法: connectionKey={}", wrapper.getConnectionKey());
                OkHttpClient httpClient = (OkHttpClient) ReflectUtil.reflectGetField(feishuClient, "httpClient");
                if (httpClient != null) {
                    httpClient.dispatcher().executorService().shutdown();
                    httpClient.connectionPool().evictAll();
                }
                log.info("飞书客户端实例已安全停止: connectionKey={}", wrapper.getConnectionKey());
            } catch (Exception e) {
                log.error("反射停止飞书客户端失败, connectionKey={}: {}", wrapper.getConnectionKey(), e.getMessage(), e);
            }
        }
    }

    /**
     * 关闭RocketMQ连接
     *
     * @param wrapper 共享连接对象
     */
    private void closeRocketMQConnection(SharedConnectionManager.SharedConnection wrapper) {
        DefaultMQPushConsumer consumer = wrapper.getRocketMQConsumer();
        if (consumer != null) {
            try {
                consumer.shutdown();
                log.info("RocketMQ消费者已关闭: connectionKey={}", wrapper.getConnectionKey());
            } catch (Exception e) {
                log.warn("关闭RocketMQ消费者异常: {}", e.getMessage());
            }
        }
    }

    /**
     * 断开所有资源连接
     */
    public void disconnectAll() {
        log.info("开始断开所有资源连接, 当前连接数: {}", sharedConnectionManager.getConnectionCount());
        sharedConnectionManager.getAllConnectionKeys().forEach(connectionKey -> {
            SharedConnectionManager.SharedConnection connection = sharedConnectionManager.getSharedConnection(connectionKey);
            if (connection != null) {
                closeConnection(connection);
            }
        });
        sharedConnectionManager.clearAll();
        triggerSubscriptionManager.clear();
        log.info("所有资源连接已断开");
    }

    /**
     * 资源类型枚举
     */
    public enum ResourceType {
        RABBITMQ,
        KAFKA,
        MQTT,
        DINGTALK,
        FEISHU,
        ROCKETMQ,
    }

    /**
     * 资源连接包装器
     */
    @Data
    public static class ResourceConnectionWrapper {
        private ResourceType type;
        private long createTime;
        private Connection rabbitMQConnection;
        private Channel rabbitMQChannel;
        private String rabbitMQConsumerTag;
        private KafkaConsumer<String, String> kafkaConsumer;
        private MqttClient mqttClient;
        private OpenDingTalkClient dingtalkClient;
        private Client feishuClient;
        private DefaultMQPushConsumer rocketMQConsumer;

    }
}
