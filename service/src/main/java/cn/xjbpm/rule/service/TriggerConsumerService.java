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

package cn.xjbpm.rule.service;

import cn.xjbpm.rule.common.utils.JsonUtils;
import cn.xjbpm.rule.consumer.MQMessageConsumer;
import cn.xjbpm.rule.custom.CredentialsManager;
import cn.xjbpm.rule.custom.EngineServices;
import cn.xjbpm.rule.engine.runtime.model.credentials.*;
import cn.xjbpm.rule.manager.ResourceCacheManager;
import cn.xjbpm.rule.node.enums.TriggerMode;
import cn.xjbpm.rule.repository.entity.StartTriggerEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 */
@Service
@Slf4j
@AllArgsConstructor
public class TriggerConsumerService implements DisposableBean {

    private final EngineServices engineServices;
    private final ResourceCacheManager resourceCacheManager;
    private final MQMessageConsumer mqMessageConsumer;


    public void addTriggerConsumers(List<StartTriggerEntity> all) {
        CredentialsManager credentialsManager = engineServices.getCredentialsManager();

        for (StartTriggerEntity ruleFlowMQEntity : all) {
            String ruleFlowKey = ruleFlowMQEntity.getRuleFlowKey();
            try {
                startConsumer(ruleFlowMQEntity, credentialsManager);
            } catch (Exception e) {
                log.error("启动MQ消费者失败: ruleFlowKey={}, type={}, error={}",
                        ruleFlowKey, ruleFlowMQEntity.getType(), e.getMessage(), e);
            }
        }
    }


    private void startConsumer(StartTriggerEntity entity, CredentialsManager credentialsManager) {
        String ruleFlowKey = entity.getRuleFlowKey();
        TriggerMode type = entity.getType();
        String content = entity.getContent();
        Map<String, String> config = JsonUtils.json2Obj(content, Map.class);
        String credentialId = config.get("credentialId");

        switch (type) {
            case RABBIT_MQ:
                startRabbitMQConsumer(ruleFlowKey, credentialId, credentialsManager, entity);
                break;
            case KAFKA:
                startKafkaConsumer(ruleFlowKey, credentialId, credentialsManager, entity);
                break;
            case MQTT:
                startMQTTConsumer(ruleFlowKey, credentialId, credentialsManager, entity);
                break;
            case DINGTALK:
                startDingtalkConsumer(ruleFlowKey, credentialId, credentialsManager, entity);
                break;
            case FEISHU:
                startFeishuConsumer(ruleFlowKey, credentialId, credentialsManager, entity);
                break;
            case ROCKETMQ:
                startRocketMQConsumer(ruleFlowKey, credentialId, credentialsManager, entity);
                break;
            default:
                log.warn("未知的类型: ruleFlowKey={}, type={}", ruleFlowKey, type);
        }
    }

    /**
     * 启动RabbitMQ消费者
     */
    private void startRabbitMQConsumer(String ruleFlowKey, String credentialId,
                                       CredentialsManager credentialsManager, StartTriggerEntity entity) {
        RabbitMQCredential credential = credentialsManager.getRabbitMQCredential(credentialId);
        if (credential == null) {
            log.error("RabbitMQ凭据不存在: ruleFlowKey={}, credentialId={}", ruleFlowKey, credentialId);
            return;
        }
        log.info("启动RabbitMQ消费者: ruleFlowKey={}, host={}:{}", ruleFlowKey, credential.getHost(), credential.getPort());
        mqMessageConsumer.startRabbitMQConsumer(ruleFlowKey, entity, credential);
    }

    /**
     * 启动Kafka消费者
     */
    private void startKafkaConsumer(String ruleFlowKey, String credentialId,
                                    CredentialsManager credentialsManager, StartTriggerEntity entity) {
        KafkaCredential credential = credentialsManager.getKafkaCredential(credentialId);
        if (credential == null) {
            log.error("Kafka凭据不存在: ruleFlowKey={}, credentialId={}", ruleFlowKey, credentialId);
            return;
        }
        log.info("启动Kafka消费者: ruleFlowKey={}, bootstrapServers={}", ruleFlowKey, credential.getBootstrapServers());
        mqMessageConsumer.startKafkaConsumer(ruleFlowKey, entity, credential);
    }

    /**
     * 启动MQTT消费者
     */
    private void startMQTTConsumer(String ruleFlowKey, String credentialId,
                                   CredentialsManager credentialsManager, StartTriggerEntity entity) {
        MQTTCredential credential = credentialsManager.getMQTTCredential(credentialId);
        if (credential == null) {
            log.error("MQTT凭据不存在: ruleFlowKey={}, credentialId={}", ruleFlowKey, credentialId);
            return;
        }
        log.info("启动MQTT消费者: ruleFlowKey={}, host={}:{}", ruleFlowKey, credential.getHost(), credential.getPort());
        mqMessageConsumer.startMQTTConsumer(ruleFlowKey, entity, credential);
    }

    /**
     * 启动钉钉消费者
     */
    private void startDingtalkConsumer(String ruleFlowKey, String credentialId,
                                       CredentialsManager credentialsManager, StartTriggerEntity entity) {
        DingtalkCredential credential = credentialsManager.getDingtalkCredential(credentialId);
        if (credential == null) {
            log.error("钉钉凭据不存在: ruleFlowKey={}, credentialId={}", ruleFlowKey, credentialId);
            return;
        }
        log.info("启动钉钉消费者: ruleFlowKey={}, clientId={}", ruleFlowKey, credential.getClientId());
        mqMessageConsumer.startDingtalkConsumer(ruleFlowKey, entity, credential);
    }

    /**
     * 启动飞书消费者
     */
    private void startFeishuConsumer(String ruleFlowKey, String credentialId,
                                     CredentialsManager credentialsManager, StartTriggerEntity entity) {
        FeishuCredential credential = credentialsManager.getFeishuCredential(credentialId);
        if (credential == null) {
            log.error("飞书凭据不存在: ruleFlowKey={}, credentialId={}", ruleFlowKey, credentialId);
            return;
        }
        log.info("飞书钉钉消费者: ruleFlowKey={}, appId={}", ruleFlowKey, credential.getAppId());
        mqMessageConsumer.startFishuConsumer(ruleFlowKey, entity, credential);
    }

    /**
     * 启动RocketMQ消费者
     */
    private void startRocketMQConsumer(String ruleFlowKey, String credentialId,
                                       CredentialsManager credentialsManager, StartTriggerEntity entity) {
        RocketMQCredential credential = credentialsManager.getRocketMQCredential(credentialId);
        if (credential == null) {
            log.error("RocketMQ凭据不存在: ruleFlowKey={}, credentialId={}", ruleFlowKey, credentialId);
            return;
        }
        log.info("启动RocketMQ消费者: ruleFlowKey={}, nameServer={}", ruleFlowKey, credential.getNameServer());
        mqMessageConsumer.startRocketMQConsumer(ruleFlowKey, entity, credential);
    }

    /**
     * 重置指定 ruleFlowKey 的所有触发器连接
     * 通常业务更新时调用此方法
     *
     * @param ruleFlowKey 规则流唯一标识
     * @param mqEntities  新的触发器配置列表
     */
    public void refreshResource(String ruleFlowKey, List<StartTriggerEntity> mqEntities) {
        log.info("刷新触发器连接: ruleFlowKey={}", ruleFlowKey);
        clearResource(ruleFlowKey);
        addTriggerConsumers(mqEntities);
    }

    /**
     * 根据ruleFlowKey 清除相关的触发器连接
     *
     * @param ruleFlowKey 规则流唯一标识
     */
    public void clearResource(String ruleFlowKey) {
        log.info("清除触发器连接: ruleFlowKey={}", ruleFlowKey);
        resourceCacheManager.disconnect(ruleFlowKey);
    }

    /**
     * 应用关闭时断开所有触发器连接
     */
    @Override
    public void destroy() throws Exception {
        log.info("TriggerConsumerService销毁，开始断开所有触发器连接...");
        resourceCacheManager.disconnectAll();
        log.info("所有触发器连接已断开");
    }
}
