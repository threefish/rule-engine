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

import cn.xjbpm.rule.common.utils.StringUtils;
import cn.xjbpm.rule.engine.runtime.model.credentials.RocketMQCredential;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalNotification;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.acl.common.AclClientRPCHook;
import org.apache.rocketmq.acl.common.SessionCredentials;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * RocketMQ客户端缓存管理器
 * 使用Guava Cache按凭据ID缓存Producer客户端实例
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Component
@Slf4j
public class RocketMQClientManager {

    /**
     * Producer客户端缓存
     * key: credentialId
     * value: DefaultMQProducer客户端实例
     */
    private final Cache<String, DefaultMQProducer> producerCache = CacheBuilder.newBuilder()
            .maximumSize(50)
            .expireAfterAccess(1, TimeUnit.HOURS)
            .removalListener((RemovalNotification<String, DefaultMQProducer> notification) -> {
                DefaultMQProducer producer = notification.getValue();
                if (producer != null) {
                    try {
                        producer.shutdown();
                        log.info("RocketMQ Producer缓存清理并关闭: credentialId={}, 原因={}",
                                notification.getKey(), notification.getCause());
                    } catch (Exception e) {
                        log.warn("RocketMQ Producer关闭失败: {}", e.getMessage());
                    }
                }
            })
            .build();

    /**
     * 获取Producer客户端（带缓存）
     *
     * @param credentialId 凭据ID
     * @param credential   RocketMQ凭据
     * @return DefaultMQProducer客户端实例
     */
    public DefaultMQProducer getOrCreateProducer(String credentialId, RocketMQCredential credential) {
        try {
            return producerCache.get(credentialId, () -> {
                log.info("创建新的RocketMQ Producer实例: credentialId={}", credentialId);
                DefaultMQProducer producer = createProducer(credential);
                producer.start();
                return producer;
            });
        } catch (Exception e) {
            log.error("获取RocketMQ Producer失败: credentialId={}, error={}",
                    credentialId, e.getMessage(), e);
            throw new RuntimeException("获取RocketMQ Producer失败: " + e.getMessage(), e);
        }
    }

    /**
     * 创建Producer
     */
    private DefaultMQProducer createProducer(RocketMQCredential credential) {
        DefaultMQProducer producer;
        
        if (StringUtils.isNotBlank(credential.getAccessKey()) && StringUtils.isNotBlank(credential.getSecretKey())) {
            SessionCredentials sessionCredentials = new SessionCredentials(
                    credential.getAccessKey(), 
                    credential.getSecretKey()
            );
            AclClientRPCHook aclClientRPCHook = new AclClientRPCHook(sessionCredentials);
            producer = new DefaultMQProducer(credential.getGroupName(), aclClientRPCHook);
        } else {
            producer = new DefaultMQProducer(credential.getGroupName());
        }
        
        producer.setNamesrvAddr(credential.getNameServer());
        producer.setSendMsgTimeout(credential.getSendTimeout());
        producer.setRetryTimesWhenSendFailed(credential.getRetryTimes());
        producer.setCompressMsgBodyOverHowmuch(credential.getCompressThreshold());

        return producer;
    }

    /**
     * 清除指定客户端缓存
     *
     * @param credentialId 凭据ID
     */
    public void invalidate(String credentialId) {
        producerCache.invalidate(credentialId);
        log.info("RocketMQ Producer缓存已清除: credentialId={}", credentialId);
    }

    /**
     * 清除所有客户端缓存
     */
    public void invalidateAll() {
        producerCache.invalidateAll();
        log.info("所有RocketMQ Producer缓存已清除");
    }

    /**
     * 获取缓存大小
     */
    public long getCacheSize() {
        return producerCache.size();
    }
}
