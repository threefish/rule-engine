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
import cn.xjbpm.rule.custom.BeanContextManager;
import cn.xjbpm.rule.custom.CredentialsManager;
import cn.xjbpm.rule.engine.runtime.model.credentials.KafkaCredential;
import cn.xjbpm.rule.engine.runtime.model.credentials.MQTTCredential;
import cn.xjbpm.rule.engine.runtime.model.credentials.RabbitMQCredential;
import cn.xjbpm.rule.manager.MQConnectionCacheManager;
import cn.xjbpm.rule.mq.MQMessageConsumer;
import cn.xjbpm.rule.repository.entity.RuleFlowMQEntity;
import cn.xjbpm.rule.repository.enums.MQType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 消息队列服务
 * 负责管理MQ连接的生命周期
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Service
@Slf4j
@AllArgsConstructor
public class MessageQueueService implements DisposableBean {

    private final BeanContextManager beanContextManager;
    private final MQConnectionCacheManager connectionCacheManager;
    private final MQMessageConsumer mqMessageConsumer;

    /**
     * 添加并启动MQ消费者
     * 每个独立的MQ链接单独进行处理，防止链接失败影响其他MQ链接
     *
     * @param all MQ配置实体列表
     */
    public void addMessageQueue(List<RuleFlowMQEntity> all) {
        CredentialsManager credentialsManager = beanContextManager.getCredentialsManager();

        for (RuleFlowMQEntity ruleFlowMQEntity : all) {
            String ruleFlowKey = ruleFlowMQEntity.getRuleFlowKey();
            try {
                startConsumer(ruleFlowMQEntity, credentialsManager);
            } catch (Exception e) {
                log.error("启动MQ消费者失败: ruleFlowKey={}, type={}, error={}",
                        ruleFlowKey, ruleFlowMQEntity.getType(), e.getMessage(), e);
            }
        }
    }

    /**
     * 启动单个MQ消费者
     *
     * @param entity MQ配置实体
     * @param credentialsManager 凭据管理器
     */
    private void startConsumer(RuleFlowMQEntity entity, CredentialsManager credentialsManager) {
        String ruleFlowKey = entity.getRuleFlowKey();
        MQType type = entity.getType();
        String content = entity.getContent();
        Map<String, String> config = JsonUtils.json2Obj(content, Map.class);
        String credentialId = config.get("credentialId");

        switch (type) {
            case RABBITMQ:
                startRabbitMQConsumer(ruleFlowKey, credentialId, credentialsManager, entity);
                break;
            case KAFKA:
                startKafkaConsumer(ruleFlowKey, credentialId, credentialsManager, entity);
                break;
            case MQTT:
                startMQTTConsumer(ruleFlowKey, credentialId, credentialsManager, entity);
                break;
            default:
                log.warn("未知的MQ类型: ruleFlowKey={}, type={}", ruleFlowKey, type);
        }
    }

    /**
     * 启动RabbitMQ消费者
     */
    private void startRabbitMQConsumer(String ruleFlowKey, String credentialId,
                                        CredentialsManager credentialsManager, RuleFlowMQEntity entity) {
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
                                     CredentialsManager credentialsManager, RuleFlowMQEntity entity) {
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
                                    CredentialsManager credentialsManager, RuleFlowMQEntity entity) {
        MQTTCredential credential = credentialsManager.getMQTTCredential(credentialId);
        if (credential == null) {
            log.error("MQTT凭据不存在: ruleFlowKey={}, credentialId={}", ruleFlowKey, credentialId);
            return;
        }
        log.info("启动MQTT消费者: ruleFlowKey={}, host={}:{}", ruleFlowKey, credential.getHost(), credential.getPort());
        mqMessageConsumer.startMQTTConsumer(ruleFlowKey, entity, credential);
    }

    /**
     * 重置指定 ruleFlowKey 的所有MQ链接
     * 通常业务更新时调用此方法
     *
     * @param ruleFlowKey 规则流唯一标识
     * @param mqEntities 新的MQ配置列表
     */
    public void refreshMessageQueue(String ruleFlowKey, List<RuleFlowMQEntity> mqEntities) {
        log.info("刷新MQ连接: ruleFlowKey={}", ruleFlowKey);
        clearMessageQueue(ruleFlowKey);
        addMessageQueue(mqEntities);
    }

    /**
     * 根据ruleFlowKey 清除相关的MQ链接
     *
     * @param ruleFlowKey 规则流唯一标识
     */
    public void clearMessageQueue(String ruleFlowKey) {
        log.info("清除MQ连接: ruleFlowKey={}", ruleFlowKey);
        connectionCacheManager.disconnect(ruleFlowKey);
    }

    /**
     * 获取当前活跃的MQ连接数量
     *
     * @return 连接数量
     */
    public int getActiveConnectionCount() {
        return connectionCacheManager.getConnectionCount();
    }

    /**
     * 检查指定规则流是否有活跃的MQ连接
     *
     * @param ruleFlowKey 规则流唯一标识
     * @return 是否有活跃连接
     */
    public boolean hasActiveConnection(String ruleFlowKey) {
        return connectionCacheManager.hasConnection(ruleFlowKey);
    }

    /**
     * 应用关闭时断开所有MQ连接
     */
    @Override
    public void destroy() throws Exception {
        log.info("MessageQueueService销毁，开始断开所有MQ连接...");
        connectionCacheManager.disconnectAll();
        log.info("所有MQ连接已断开");
    }
}
