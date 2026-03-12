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

import cn.xjbpm.rule.engine.runtime.model.credentials.MQTTCredential;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * MQTT 工具类（消息发布者）
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Slf4j
public class MQTTUtils {

    /**
     * 发布消息到MQTT
     *
     * @param credential MQTT凭据
     * @param topic      主题
     * @param message    消息内容
     * @param qos        服务质量等级
     * @param retained   是否保留消息
     * @return 操作结果
     */
    public static MQTTResult publish(MQTTCredential credential, String topic, String message,
                                     int qos, boolean retained) {
        String clientId = "mqtt-publisher-" + UUID.randomUUID().toString().substring(0, 8);
        MqttClient client = null;

        try {
            client = createClient(credential, clientId);
            client.connect(createConnectOptions(credential));

            MqttMessage mqttMessage = new MqttMessage(message.getBytes(StandardCharsets.UTF_8));
            mqttMessage.setQos(qos);
            mqttMessage.setRetained(retained);

            client.publish(topic, mqttMessage);

            log.info("MQTT消息发布成功: topic={}, qos={}", topic, qos);

            return MQTTResult.builder()
                    .success(true)
                    .topic(topic)
                    .build();

        } catch (Exception e) {
            log.error("MQTT消息发布失败: {}", e.getMessage(), e);
            return MQTTResult.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();
        } finally {
            disconnect(client);
        }
    }

    /**
     * 创建MQTT客户端
     */
    private static MqttClient createClient(MQTTCredential credential, String clientId) throws MqttException {
        String protocol = credential.isSsl() ? "ssl" : "tcp";
        String brokerUrl = String.format("%s://%s:%d", protocol, credential.getHost(), credential.getPort());
        return new MqttClient(brokerUrl, clientId, new MemoryPersistence());
    }

    /**
     * 创建连接选项
     */
    private static MqttConnectOptions createConnectOptions(MQTTCredential credential) {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
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

    /**
     * 断开连接
     */
    private static void disconnect(MqttClient client) {
        if (client != null) {
            try {
                if (client.isConnected()) {
                    client.disconnect();
                }
                client.close();
            } catch (MqttException e) {
                log.warn("MQTT断开连接异常: {}", e.getMessage());
            }
        }
    }

    /**
     * MQTT操作结果
     */
    @lombok.Builder
    @lombok.Data
    public static class MQTTResult {
        private boolean success;
        private String errorMessage;
        private String topic;
    }
}
