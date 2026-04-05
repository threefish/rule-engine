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

package cn.xjbpm.rule.consumer;

import cn.hutool.core.util.IdUtil;
import cn.xjbpm.rule.common.utils.JsonUtils;
import cn.xjbpm.rule.dispatcher.MessageDispatcher;
import cn.xjbpm.rule.dispatcher.SubscriberInfo;
import cn.xjbpm.rule.dispatcher.TriggerSubscriptionManager;
import cn.xjbpm.rule.engine.runtime.model.credentials.*;
import cn.xjbpm.rule.manager.ResourceCacheManager;
import cn.xjbpm.rule.manager.SharedConnectionManager;
import cn.xjbpm.rule.node.enums.TriggerMode;
import cn.xjbpm.rule.repository.entity.StartTriggerEntity;
import com.dingtalk.open.app.api.OpenDingTalkClient;
import com.dingtalk.open.app.api.OpenDingTalkStreamClientBuilder;
import com.dingtalk.open.app.api.security.AuthClientCredential;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.lark.oapi.event.EventDispatcher;
import com.lark.oapi.service.im.ImService;
import com.lark.oapi.service.im.v1.model.EventMessage;
import com.lark.oapi.service.im.v1.model.EventSender;
import com.lark.oapi.service.im.v1.model.P2MessageReceiveV1;
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
import org.apache.rocketmq.acl.common.AclClientRPCHook;
import org.apache.rocketmq.acl.common.SessionCredentials;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.protocol.heartbeat.MessageModel;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

/**
 * 消息消费者
 * 负责建立消息连接并消费消息，触发规则流执行
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Component
@Slf4j
@AllArgsConstructor
@SuppressWarnings("all")
public class MQMessageConsumer {


    private final ResourceCacheManager connectionCacheManager;
    private final ProcessMessageHelper processMessageService;
    private final SharedConnectionManager sharedConnectionManager;
    private final TriggerSubscriptionManager triggerSubscriptionManager;
    private final MessageDispatcher messageDispatcher;


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
    public void startRabbitMQConsumer(String ruleFlowKey, StartTriggerEntity entity, RabbitMQCredential credential) {
        Map<String, String> config = JsonUtils.json2Obj(entity.getContent(), Map.class);
        String connectionKey = sharedConnectionManager.generateConnectionKey(TriggerMode.RABBIT_MQ, config);

        SubscriberInfo subscriberInfo = new SubscriberInfo(
                ruleFlowKey,
                entity.getFilterRule(),
                entity.getFilterType()
        );
        triggerSubscriptionManager.addSubscription(connectionKey, subscriberInfo);

        boolean needCreateConnection = sharedConnectionManager.subscribe(connectionKey, ruleFlowKey, ResourceCacheManager.ResourceType.RABBITMQ);

        if (!needCreateConnection) {
            log.info("RabbitMQ连接已存在，共享连接: ruleFlowKey={}, connectionKey={}", ruleFlowKey, connectionKey);
            return;
        }

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

                String finalConnectionKey = connectionKey;
                DeliverCallback deliverCallback = (consumerTag1, delivery) -> {
                    String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                    log.info("RabbitMQ收到消息: connectionKey={}, queue={}, message={}", finalConnectionKey, queueName, message);
                    List<SubscriberInfo> subscribers = triggerSubscriptionManager.getSubscribers(finalConnectionKey);
                    messageDispatcher.dispatch(finalConnectionKey, message, subscribers);
                };

                channel.basicConsume(queueName, autoAck, consumerTag, deliverCallback, consumerTag1 -> {
                    log.info("RabbitMQ消费者被取消: connectionKey={}, consumerTag={}", finalConnectionKey, consumerTag1);
                });

                sharedConnectionManager.setRabbitMQConnection(connectionKey, connection, channel, consumerTag);
                log.info("RabbitMQ消费者启动成功: ruleFlowKey={}, queue={}, connectionKey={}", ruleFlowKey, queueName, connectionKey);

            } catch (IOException | TimeoutException e) {
                log.error("RabbitMQ消费者启动失败: connectionKey={}, error={}", connectionKey, e.getMessage(), e);
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
    public void startKafkaConsumer(String ruleFlowKey, StartTriggerEntity entity, KafkaCredential credential) {
        Map<String, String> config = JsonUtils.json2Obj(entity.getContent(), Map.class);
        String connectionKey = sharedConnectionManager.generateConnectionKey(TriggerMode.KAFKA, config);

        SubscriberInfo subscriberInfo = new SubscriberInfo(
                ruleFlowKey,
                entity.getFilterRule(),
                entity.getFilterType()
        );
        triggerSubscriptionManager.addSubscription(connectionKey, subscriberInfo);

        boolean needCreateConnection = sharedConnectionManager.subscribe(connectionKey, ruleFlowKey, ResourceCacheManager.ResourceType.KAFKA);

        if (!needCreateConnection) {
            log.info("Kafka连接已存在，共享连接: ruleFlowKey={}, connectionKey={}", ruleFlowKey, connectionKey);
            return;
        }

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

                sharedConnectionManager.setKafkaConnection(connectionKey, consumer);
                log.info("Kafka消费者启动成功: ruleFlowKey={}, topic={}, groupId={}, connectionKey={}", ruleFlowKey, topic, groupId, connectionKey);

                while (sharedConnectionManager.hasConnection(connectionKey)) {
                    try {
                        ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(timeout));
                        for (ConsumerRecord<String, String> record : records) {
                            String message = record.value();
                            log.info("Kafka收到消息: connectionKey={}, topic={}, partition={}, offset={}, message={}", connectionKey, topic, record.partition(), record.offset(), message);
                            List<SubscriberInfo> subscribers = triggerSubscriptionManager.getSubscribers(connectionKey);
                            messageDispatcher.dispatch(connectionKey, message, subscribers);
                        }
                    } catch (Exception e) {
                        if (e instanceof org.apache.kafka.common.errors.WakeupException) {
                            log.info("Kafka消费者收到唤醒信号，准备退出: connectionKey={}", connectionKey);
                            break;
                        }
                        log.error("Kafka消费异常: connectionKey={}, error={}", connectionKey, e.getMessage());
                    }
                }

            } catch (Exception e) {
                log.error("Kafka消费者启动失败: connectionKey={}, error={}", connectionKey, e.getMessage(), e);
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
    public void startMQTTConsumer(String ruleFlowKey, StartTriggerEntity entity, MQTTCredential credential) {
        Map<String, String> config = JsonUtils.json2Obj(entity.getContent(), Map.class);
        String connectionKey = sharedConnectionManager.generateConnectionKey(TriggerMode.MQTT, config);

        SubscriberInfo subscriberInfo = new SubscriberInfo(
                ruleFlowKey,
                entity.getFilterRule(),
                entity.getFilterType()
        );
        triggerSubscriptionManager.addSubscription(connectionKey, subscriberInfo);

        boolean needCreateConnection = sharedConnectionManager.subscribe(connectionKey, ruleFlowKey, ResourceCacheManager.ResourceType.MQTT);

        if (!needCreateConnection) {
            log.info("MQTT连接已存在，共享连接: ruleFlowKey={}, connectionKey={}", ruleFlowKey, connectionKey);
            return;
        }

        String topic = config.get("topic");
        int qos = Integer.parseInt(config.getOrDefault("qos", "1"));
        boolean cleanSession = Boolean.parseBoolean(config.getOrDefault("cleanSession", "true"));
        String clientId = config.get("clientId");
        if (clientId == null || clientId.isEmpty()) {
            clientId = "rule-flow-mqtt-" + ruleFlowKey + "-" + IdUtil.getSnowflakeNextIdStr();
        }

        String finalClientId = clientId;
        String finalConnectionKey = connectionKey;
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
                        log.warn("MQTT连接丢失: connectionKey={}, error={}", finalConnectionKey, cause.getMessage());
                        handleMQTTReconnect(finalConnectionKey, mqttClient, options);
                    }

                    @Override
                    public void messageArrived(String topic1, MqttMessage message) {
                        String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
                        log.info("MQTT收到消息: connectionKey={}, topic={}, message={}", finalConnectionKey, topic1, payload);
                        List<SubscriberInfo> subscribers = triggerSubscriptionManager.getSubscribers(finalConnectionKey);
                        messageDispatcher.dispatch(finalConnectionKey, payload, subscribers);
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {
                    }
                });

                mqttClient.subscribe(topic, qos);
                sharedConnectionManager.setMQTTConnection(connectionKey, mqttClient);
                log.info("MQTT消费者启动成功: ruleFlowKey={}, topic={}, clientId={}, connectionKey={}", ruleFlowKey, topic, finalClientId, connectionKey);

            } catch (MqttException e) {
                log.error("MQTT消费者启动失败: connectionKey={}, error={}", connectionKey, e.getMessage(), e);
            }
        });
    }

    /**
     * 启动钉钉消费者
     *
     * @param ruleFlowKey 规则流唯一标识
     * @param entity      MQ配置实体
     * @param credential  钉钉凭据
     */
    public void startDingtalkConsumer(String ruleFlowKey, StartTriggerEntity entity, DingtalkCredential credential) {
        Map<String, String> config = JsonUtils.json2Obj(entity.getContent(), Map.class);
        String connectionKey = sharedConnectionManager.generateConnectionKey(TriggerMode.DINGTALK, config);

        SubscriberInfo subscriberInfo = new SubscriberInfo(
                ruleFlowKey,
                entity.getFilterRule(),
                entity.getFilterType()
        );
        triggerSubscriptionManager.addSubscription(connectionKey, subscriberInfo);

        boolean needCreateConnection = sharedConnectionManager.subscribe(connectionKey, ruleFlowKey, ResourceCacheManager.ResourceType.DINGTALK);

        if (!needCreateConnection) {
            log.info("钉钉连接已存在，共享连接: ruleFlowKey={}, connectionKey={}", ruleFlowKey, connectionKey);
            return;
        }

        executorService.submit(() -> {
            try {
                DingtalkMsgCallbackConsumer callbackConsumer = new DingtalkMsgCallbackConsumer(
                        processMessageService,
                        connectionKey,
                        triggerSubscriptionManager,
                        messageDispatcher
                );
                OpenDingTalkClient client = OpenDingTalkStreamClientBuilder
                        .custom()
                        .credential(new AuthClientCredential(credential.getClientId(), credential.getClientSecret()))
                        .registerCallbackListener("/v1.0/im/bot/messages/get", callbackConsumer)
                        .build();
                client.start();
                sharedConnectionManager.setDingtalkConnection(connectionKey, client);
                log.info("钉钉消费者启动成功: ruleFlowKey={}, clientId={}, connectionKey={}", ruleFlowKey, credential.getClientId(), connectionKey);
            } catch (Exception e) {
                log.error("钉钉消费者启动失败: connectionKey={}, error={}", connectionKey, e.getMessage(), e);
            }
        });
    }

    /**
     * 启动飞书消费者
     *
     * @param ruleFlowKey 规则流唯一标识
     * @param entity      MQ配置实体
     * @param credential  飞书凭据
     */
    public void startFishuConsumer(String ruleFlowKey, StartTriggerEntity entity, FeishuCredential credential) {
        Map<String, String> config = JsonUtils.json2Obj(entity.getContent(), Map.class);
        String connectionKey = sharedConnectionManager.generateConnectionKey(TriggerMode.FEISHU, config);

        SubscriberInfo subscriberInfo = new SubscriberInfo(
                ruleFlowKey,
                entity.getFilterRule(),
                entity.getFilterType()
        );
        triggerSubscriptionManager.addSubscription(connectionKey, subscriberInfo);

        boolean needCreateConnection = sharedConnectionManager.subscribe(connectionKey, ruleFlowKey, ResourceCacheManager.ResourceType.FEISHU);

        if (!needCreateConnection) {
            log.info("飞书连接已存在，共享连接: ruleFlowKey={}, connectionKey={}", ruleFlowKey, connectionKey);
            return;
        }

        executorService.submit(() -> {
            try {
                FeishuMsgCallbackConsumer feishuMsgCallbackConsumer = new FeishuMsgCallbackConsumer(
                        processMessageService,
                        connectionKey,
                        triggerSubscriptionManager,
                        messageDispatcher
                );
                EventDispatcher eventDispatcher = EventDispatcher.newBuilder("", "")
                        .onP2MessageReceiveV1(new ImService.P2MessageReceiveV1Handler() {
                            @Override
                            public void handle(P2MessageReceiveV1 event) throws Exception {
                                EventSender sender = event.getEvent().getSender();
                                EventMessage message = event.getEvent().getMessage();
                                Map<String, Object> data = new HashMap<>();
                                data.put("sender", sender);
                                data.put("message", message);
                                feishuMsgCallbackConsumer.handleCallback(data);
                            }
                        })
                        .build();
                com.lark.oapi.ws.Client wsClient = new com.lark.oapi.ws.Client.Builder(credential.getAppId(), credential.getAppSecret())
                        .eventHandler(eventDispatcher).build();
                wsClient.start();
                sharedConnectionManager.setFeishuConnection(connectionKey, wsClient);
                log.info("飞书消费者启动成功: ruleFlowKey={}, appId={}, connectionKey={}", ruleFlowKey, credential.getAppId(), connectionKey);
            } catch (Exception e) {
                log.error("飞书消费者启动失败: connectionKey={}, error={}", connectionKey, e.getMessage(), e);
            }
        });
    }

    /**
     * 启动RocketMQ消费者
     *
     * @param ruleFlowKey 规则流唯一标识
     * @param entity      MQ配置实体
     * @param credential  RocketMQ凭据
     */
    public void startRocketMQConsumer(String ruleFlowKey, StartTriggerEntity entity, RocketMQCredential credential) {
        Map<String, String> config = JsonUtils.json2Obj(entity.getContent(), Map.class);
        String connectionKey = sharedConnectionManager.generateConnectionKey(TriggerMode.ROCKETMQ, config);

        SubscriberInfo subscriberInfo = new SubscriberInfo(
                ruleFlowKey,
                entity.getFilterRule(),
                entity.getFilterType()
        );
        triggerSubscriptionManager.addSubscription(connectionKey, subscriberInfo);

        boolean needCreateConnection = sharedConnectionManager.subscribe(connectionKey, ruleFlowKey, ResourceCacheManager.ResourceType.ROCKETMQ);

        if (!needCreateConnection) {
            log.info("RocketMQ连接已存在，共享连接: ruleFlowKey={}, connectionKey={}", ruleFlowKey, connectionKey);
            return;
        }

        String topic = config.get("topic");
        String groupId = config.get("groupId");
        String consumeMode = config.getOrDefault("consumeMode", "CLUSTERING");
        int consumeThread = Integer.parseInt(config.getOrDefault("consumeThread", "20"));

        executorService.submit(() -> {
            try {
                DefaultMQPushConsumer consumer = createRocketMQConsumer(credential, groupId, consumeMode, consumeThread);
                consumer.subscribe(topic, "*");
                String finalConnectionKey = connectionKey;
                consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
                    for (MessageExt msg : msgs) {
                        String message = new String(msg.getBody(), StandardCharsets.UTF_8);
                        log.info("RocketMQ收到消息: connectionKey={}, topic={}, tags={}, keys={}, message={}",
                                finalConnectionKey, msg.getTopic(), msg.getTags(), msg.getKeys(), message);
                        List<SubscriberInfo> subscribers = triggerSubscriptionManager.getSubscribers(finalConnectionKey);
                        messageDispatcher.dispatch(finalConnectionKey, message, subscribers);
                    }
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                });

                consumer.start();
                sharedConnectionManager.setRocketMQConnection(connectionKey, consumer);
                log.info("RocketMQ消费者启动成功: ruleFlowKey={}, topic={}, groupId={}, connectionKey={}", ruleFlowKey, topic, groupId, connectionKey);

            } catch (Exception e) {
                log.error("RocketMQ消费者启动失败: connectionKey={}, error={}", connectionKey, e.getMessage(), e);
            }
        });
    }

    /**
     * 创建RocketMQ消费者
     */
    private DefaultMQPushConsumer createRocketMQConsumer(RocketMQCredential credential, String groupId, String consumeMode, int consumeThread) throws Exception {
        DefaultMQPushConsumer consumer;
        if (org.apache.commons.lang3.StringUtils.isNotBlank(credential.getAccessKey()) && org.apache.commons.lang3.StringUtils.isNotBlank(credential.getSecretKey())) {
            SessionCredentials sessionCredentials = new SessionCredentials(credential.getAccessKey(), credential.getSecretKey());
            AclClientRPCHook aclClientRPCHook = new AclClientRPCHook(sessionCredentials);
            consumer = new DefaultMQPushConsumer(aclClientRPCHook);
            consumer.setConsumerGroup(groupId);
        } else {
            consumer = new DefaultMQPushConsumer(groupId);
        }
        consumer.setNamesrvAddr(credential.getNameServer());
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        consumer.setConsumeThreadMin(consumeThread);
        consumer.setConsumeThreadMax(consumeThread);
        if (MessageModel.BROADCASTING.name().equalsIgnoreCase(consumeMode)) {
            consumer.setMessageModel(MessageModel.BROADCASTING);
        } else {
            consumer.setMessageModel(MessageModel.CLUSTERING);
        }
        return consumer;
    }


    /**
     * 处理MQTT重连
     *
     * @param connectionKey 连接标识
     * @param mqttClient    MQTT客户端
     * @param options       MQTT连接选项
     */
    private void handleMQTTReconnect(String connectionKey, MqttClient mqttClient, MqttConnectOptions options) {
        int retryCount = 0;
        int maxRetries = 5;
        while (retryCount < maxRetries && sharedConnectionManager.hasConnection(connectionKey)) {
            try {
                Thread.sleep(5000);
                if (!mqttClient.isConnected()) {
                    mqttClient.connect(options);
                    log.info("MQTT重连成功: connectionKey={}", connectionKey);
                    return;
                }
            } catch (Exception e) {
                retryCount++;
                log.warn("MQTT重连失败: connectionKey={}, retry={}/{}, error={}", connectionKey, retryCount, maxRetries, e.getMessage());
            }
        }
        if (retryCount >= maxRetries) {
            log.error("MQTT重连失败，已达到最大重试次数: connectionKey={}", connectionKey);
        }
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
