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

import cn.xjbpm.rule.engine.definition.model.nodes.KafkaNode;
import cn.xjbpm.rule.engine.runtime.model.credentials.KafkaCredential;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.serialization.StringSerializer;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Kafka 工具类（消息生产者）
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Slf4j
public class KafkaUtils {

    /**
     * 生产消息到Kafka
     *
     * @param credential Kafka凭据
     * @param topic 主题
     * @param messageKey 消息键
     * @param messageValue 消息值
     * @param ackMode 确认模式
     * @param partition 分区
     * @param headers 消息头JSON
     * @return 操作结果
     */
    public static KafkaResult produce(KafkaCredential credential, String topic, String messageKey,
                                       String messageValue, KafkaNode.AckMode ackMode,
                                       Integer partition, String headers) {
        Properties props = createProducerProperties(credential, ackMode);

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            ProducerRecord<String, String> record = createProducerRecord(topic, messageKey, messageValue, partition, headers);

            RecordMetadata metadata = producer.send(record).get();

            log.info("Kafka消息发送成功: topic={}, partition={}, offset={}",
                    metadata.topic(), metadata.partition(), metadata.offset());

            return KafkaResult.builder()
                    .success(true)
                    .topic(metadata.topic())
                    .partition(metadata.partition())
                    .offset(metadata.offset())
                    .build();

        } catch (Exception e) {
            log.error("Kafka消息发送失败: {}", e.getMessage(), e);
            return KafkaResult.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    /**
     * 创建生产者配置
     */
    private static Properties createProducerProperties(KafkaCredential credential, KafkaNode.AckMode ackMode) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, credential.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        switch (ackMode) {
            case NONE:
                props.put(ProducerConfig.ACKS_CONFIG, "0");
                break;
            case LEADER:
                props.put(ProducerConfig.ACKS_CONFIG, "1");
                break;
            case ALL:
                props.put(ProducerConfig.ACKS_CONFIG, "all");
                break;
        }

        configureSecurity(props, credential);

        return props;
    }

    /**
     * 配置安全参数
     */
    private static void configureSecurity(Properties props, KafkaCredential credential) {
        String securityProtocol = credential.getSecurityProtocol();
        if (StringUtils.isNotBlank(securityProtocol) && !"PLAINTEXT".equals(securityProtocol)) {
            props.put("security.protocol", securityProtocol);

            if (StringUtils.isNotBlank(credential.getSaslMechanism())) {
                props.put("sasl.mechanism", credential.getSaslMechanism());
            }

            if (StringUtils.isNotBlank(credential.getUsername()) && StringUtils.isNotBlank(credential.getPassword())) {
                String jaasConfig = String.format(
                        "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"%s\" password=\"%s\";",
                        credential.getUsername(), credential.getPassword()
                );
                props.put("sasl.jaas.config", jaasConfig);
            }
        }
    }

    /**
     * 创建生产者记录
     */
    private static ProducerRecord<String, String> createProducerRecord(String topic, String key,
                                                                         String value, Integer partition,
                                                                         String headers) {
        Headers kafkaHeaders = parseHeaders(headers);

        if (partition != null) {
            return new ProducerRecord<>(topic, partition, key, value, kafkaHeaders);
        } else {
            return new ProducerRecord<>(topic, null, key, value, kafkaHeaders);
        }
    }

    /**
     * 解析消息头
     */
    private static Headers parseHeaders(String headers) {
        RecordHeaders recordHeaders = new RecordHeaders();

        if (StringUtils.isNotBlank(headers)) {
            try {
                Map<String, Object> headersMap = JsonUtils.json2Obj(headers, Map.class);
                if (headersMap != null) {
                    headersMap.forEach((k, v) -> {
                        if (v != null) {
                            recordHeaders.add(k, v.toString().getBytes(StandardCharsets.UTF_8));
                        }
                    });
                }
            } catch (Exception e) {
                log.warn("解析消息头失败: {}", e.getMessage());
            }
        }

        return recordHeaders;
    }

    /**
     * Kafka操作结果
     */
    @lombok.Builder
    @lombok.Data
    public static class KafkaResult {
        private boolean success;
        private String errorMessage;
        private String topic;
        private int partition;
        private long offset;
    }
}
