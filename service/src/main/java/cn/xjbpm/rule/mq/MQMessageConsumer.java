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

package cn.xjbpm.rule.mq;

import cn.hutool.core.util.IdUtil;
import cn.xjbpm.rule.common.utils.JsonUtils;
import cn.xjbpm.rule.dto.ExcuteRuleFlow;
import cn.xjbpm.rule.engine.runtime.RuleFlowExcuteService;
import cn.xjbpm.rule.engine.runtime.model.credentials.KafkaCredential;
import cn.xjbpm.rule.engine.runtime.model.credentials.MQTTCredential;
import cn.xjbpm.rule.engine.runtime.model.credentials.RabbitMQCredential;
import cn.xjbpm.rule.manager.MQConnectionCacheManager;
import cn.xjbpm.rule.repository.entity.RuleFlowMQEntity;
import cn.xjbpm.rule.utils.SpringContextUtil;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.*;

/**
 * MQ消息消费者
 * 负责建立MQ连接并消费消息，触发规则流执行
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Component
@Slf4j
@AllArgsConstructor
@SuppressWarnings("all")
public class MQMessageConsumer {


    private final MQConnectionCacheManager connectionCacheManager;
    private final ExecutorService executorService = new ThreadPoolExecutor(
            10, 50, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(100),
            new ThreadFactoryBuilder().setNameFormat("mq-consumer-%d").build(),
            new ThreadPoolExecutor.AbortPolicy()
    );

    /**
     * 启动RabbitMQ消费者
     *
     * @param ruleFlowKey 规则流唯一标识
     * @param entity      MQ配置实体
     * @param credential  RabbitMQ凭据
     */
    public void startRabbitMQConsumer(String ruleFlowKey, RuleFlowMQEntity entity, RabbitMQCredential credential) {
        Map<String, String> config = JsonUtils.json2Obj(entity.getContent(), Map.class);
        String queueName = config.get("queueName");
        boolean autoAck = Boolean.parseBoolean(config.get("autoAck"));
        int prefetchCount = Integer.parseInt(config.getOrDefault("prefetchCount", "1"));

        executorService.submit(() -> {
            Connection connection = null;
            Channel channel = null;
            try {
                ConnectionFactory factory = createRabbitMQConnectionFactory(credential);
                connection = factory.newConnection();
                channel = connection.createChannel();

                channel.basicQos(prefetchCount);
                channel.queueDeclare(queueName, true, false, false, null);

                String consumerTag = "rule-flow-consumer-" + ruleFlowKey + "-" + IdUtil.getSnowflakeNextIdStr();

                DeliverCallback deliverCallback = (consumerTag1, delivery) -> {
                    String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                    log.info("RabbitMQ收到消息: ruleFlowKey={}, queue={}, message={}", ruleFlowKey, queueName, message);
                    processMessage(ruleFlowKey, message);
                };

                channel.basicConsume(queueName, autoAck, consumerTag, deliverCallback, consumerTag1 -> {
                    log.info("RabbitMQ消费者被取消: ruleFlowKey={}, consumerTag={}", ruleFlowKey, consumerTag1);
                });

                connectionCacheManager.addRabbitMQConnection(ruleFlowKey, connection, channel, consumerTag);
                log.info("RabbitMQ消费者启动成功: ruleFlowKey={}, queue={}", ruleFlowKey, queueName);

            } catch (IOException | TimeoutException e) {
                log.error("RabbitMQ消费者启动失败: ruleFlowKey={}, error={}", ruleFlowKey, e.getMessage(), e);
                closeResource(channel, connection);
            }
        });
    }

    /**
     * 启动Kafka消费者
     *
     * @param ruleFlowKey 规则流唯一标识
     * @param entity      MQ配置实体
     * @param credential  Kafka凭据
     */
    public void startKafkaConsumer(String ruleFlowKey, RuleFlowMQEntity entity, KafkaCredential credential) {
        Map<String, String> config = JsonUtils.json2Obj(entity.getContent(), Map.class);
        String topic = config.get("topic");
        String groupId = config.get("groupId");
        String offsetReset = config.getOrDefault("offsetReset", "EARLIEST");
        boolean autoCommit = Boolean.parseBoolean(config.getOrDefault("autoCommit", "true"));
        long timeout = Long.parseLong(config.getOrDefault("timeout", "1"));
        int maxPollRecords = Integer.parseInt(config.getOrDefault("maxPollRecords", "10"));

        executorService.submit(() -> {
            try {
                Properties props = createKafkaConsumerProperties(credential, groupId, offsetReset, autoCommit, maxPollRecords);
                KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
                consumer.subscribe(Collections.singletonList(topic));

                connectionCacheManager.addKafkaConnection(ruleFlowKey, consumer);
                log.info("Kafka消费者启动成功: ruleFlowKey={}, topic={}, groupId={}", ruleFlowKey, topic, groupId);

                while (connectionCacheManager.hasConnection(ruleFlowKey)) {
                    try {
                        ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(timeout));
                        for (ConsumerRecord<String, String> record : records) {
                            String message = record.value();
                            log.info("Kafka收到消息: ruleFlowKey={}, topic={}, partition={}, offset={}, message={}", ruleFlowKey, topic, record.partition(), record.offset(), message);
                            processMessage(ruleFlowKey, message);
                        }
                    } catch (Exception e) {
                        if (e instanceof org.apache.kafka.common.errors.WakeupException) {
                            log.info("Kafka消费者收到唤醒信号，准备退出: ruleFlowKey={}", ruleFlowKey);
                            break;
                        }
                        log.error("Kafka消费异常: ruleFlowKey={}, error={}", ruleFlowKey, e.getMessage());
                    }
                }

            } catch (Exception e) {
                log.error("Kafka消费者启动失败: ruleFlowKey={}, error={}", ruleFlowKey, e.getMessage(), e);
            }
        });
    }

    /**
     * 启动MQTT消费者
     *
     * @param ruleFlowKey 规则流唯一标识
     * @param entity      MQ配置实体
     * @param credential  MQTT凭据
     */
    public void startMQTTConsumer(String ruleFlowKey, RuleFlowMQEntity entity, MQTTCredential credential) {
        Map<String, String> config = JsonUtils.json2Obj(entity.getContent(), Map.class);
        String topic = config.get("topic");
        int qos = Integer.parseInt(config.getOrDefault("qos", "1"));
        boolean cleanSession = Boolean.parseBoolean(config.getOrDefault("cleanSession", "true"));
        String clientId = config.get("clientId");
        if (clientId == null || clientId.isEmpty()) {
            clientId = "rule-flow-mqtt-" + ruleFlowKey + "-" + IdUtil.getSnowflakeNextIdStr();
        }

        String finalClientId = clientId;
        executorService.submit(() -> {
            try {
                String protocol = credential.isSsl() ? "ssl" : "tcp";
                String brokerUrl = String.format("%s://%s:%d", protocol, credential.getHost(), credential.getPort());
                MqttClient mqttClient = new MqttClient(brokerUrl, finalClientId, new MemoryPersistence());

                MqttConnectOptions options = createMQTTConnectOptions(credential, cleanSession);
                mqttClient.connect(options);

                mqttClient.setCallback(new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable cause) {
                        log.warn("MQTT连接丢失: ruleFlowKey={}, error={}", ruleFlowKey, cause.getMessage());
                        handleMQTTReconnect(ruleFlowKey, mqttClient, options);
                    }

                    @Override
                    public void messageArrived(String topic1, MqttMessage message) {
                        String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
                        log.info("MQTT收到消息: ruleFlowKey={}, topic={}, message={}", ruleFlowKey, topic1, payload);
                        processMessage(ruleFlowKey, payload);
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {
                    }
                });

                mqttClient.subscribe(topic, qos);
                connectionCacheManager.addMQTTConnection(ruleFlowKey, mqttClient);
                log.info("MQTT消费者启动成功: ruleFlowKey={}, topic={}, clientId={}", ruleFlowKey, topic, finalClientId);

            } catch (MqttException e) {
                log.error("MQTT消费者启动失败: ruleFlowKey={}, error={}", ruleFlowKey, e.getMessage(), e);
            }
        });
    }

    /**
     * 处理MQTT重连
     */
    private void handleMQTTReconnect(String ruleFlowKey, MqttClient mqttClient, MqttConnectOptions options) {
        int retryCount = 0;
        int maxRetries = 5;
        while (retryCount < maxRetries && connectionCacheManager.hasConnection(ruleFlowKey)) {
            try {
                Thread.sleep(5000);
                if (!mqttClient.isConnected()) {
                    mqttClient.connect(options);
                    log.info("MQTT重连成功: ruleFlowKey={}", ruleFlowKey);
                    return;
                }
            } catch (Exception e) {
                retryCount++;
                log.warn("MQTT重连失败: ruleFlowKey={}, retry={}/{}, error={}", ruleFlowKey, retryCount, maxRetries, e.getMessage());
            }
        }
        if (retryCount >= maxRetries) {
            log.error("MQTT重连失败，已达到最大重试次数: ruleFlowKey={}", ruleFlowKey);
        }
    }

    /**
     * 处理收到的消息并触发规则流执行
     *
     * @param ruleFlowKey 规则流唯一标识
     * @param message     消息内容
     */
    private void processMessage(String ruleFlowKey, String message) {
        try {
            Map<String, Object> variables = parseMessageToVariables(message);
            ExcuteRuleFlow excuteRuleFlow = new ExcuteRuleFlow();
            excuteRuleFlow.setKey(ruleFlowKey);
            excuteRuleFlow.setRequestId(IdUtil.getSnowflakeNextIdStr());
            excuteRuleFlow.setVariables(variables);
            excuteRuleFlow.setAsyncExcute(true);
            SpringContextUtil.getBean(RuleFlowExcuteService.class).startFlow(excuteRuleFlow);
        } catch (Exception e) {
            log.error("处理MQ消息失败: ruleFlowKey={}, error={}", ruleFlowKey, e.getMessage(), e);
        }
    }

    /**
     * 解析消息内容为变量Map
     */
    private Map<String, Object> parseMessageToVariables(String message) {
        Map<String, Object> variables = new HashMap<>();
        try {
            Map<String, Object> parsed = JsonUtils.json2Obj(message, Map.class);
            if (parsed != null) {
                variables.putAll(parsed);
            }
        } catch (Exception e) {
            variables.put("message", message);
        }
        return variables;
    }

    /**
     * 创建RabbitMQ连接工厂
     */
    private ConnectionFactory createRabbitMQConnectionFactory(RabbitMQCredential credential) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(credential.getHost());
        factory.setPort(credential.getPort());
        factory.setUsername(credential.getUsername());
        factory.setPassword(credential.getPassword());
        factory.setVirtualHost(credential.getVirtualHost());
        factory.setAutomaticRecoveryEnabled(true);
        factory.setNetworkRecoveryInterval(5000);
        return factory;
    }

    /**
     * 创建Kafka消费者配置
     */
    private Properties createKafkaConsumerProperties(KafkaCredential credential, String groupId, String offsetReset, boolean autoCommit, int maxPollRecords) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, credential.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, autoCommit);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offsetReset.toLowerCase());
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);


        String securityProtocol = credential.getSecurityProtocol();
        if (securityProtocol != null && !"PLAINTEXT".equals(securityProtocol)) {
            props.put("security.protocol", securityProtocol);
            if (credential.getSaslMechanism() != null) {
                props.put("sasl.mechanism", credential.getSaslMechanism());
            }
            if (credential.getUsername() != null && credential.getPassword() != null) {
                String jaasConfig = String.format("org.apache.kafka.common.security.plain.PlainLoginModule required username=\"%s\" password=\"%s\";", credential.getUsername(), credential.getPassword());
                props.put("sasl.jaas.config", jaasConfig);
            }
        }
        return props;
    }

    /**
     * 创建MQTT连接选项
     */
    private MqttConnectOptions createMQTTConnectOptions(MQTTCredential credential, boolean cleanSession) {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(cleanSession);
        options.setAutomaticReconnect(true);
        options.setConnectionTimeout(30);
        options.setKeepAliveInterval(60);

        if (credential.getUsername() != null && !credential.getUsername().isEmpty()) {
            options.setUserName(credential.getUsername());
        }
        if (credential.getPassword() != null && !credential.getPassword().isEmpty()) {
            options.setPassword(credential.getPassword().toCharArray());
        }
        return options;
    }

    private void closeResource(AutoCloseable... resources) {
        for (AutoCloseable res : resources) {
            if (res != null) {
                try {
                    res.close();
                } catch (Exception ignored) {
                }
            }
        }
    }
}
