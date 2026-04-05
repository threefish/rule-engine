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

import cn.xjbpm.rule.engine.runtime.model.credentials.OcrCredential;
import com.baidu.aip.ocr.AipOcr;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalNotification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * OCR客户端管理器
 * 使用Guava Cache按凭据ID缓存AipOcr客户端实例
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Component
@Slf4j
public class OcrClientManager {

    /**
     * OCR客户端缓存
     * key: credentialId
     * value: AipOcr客户端实例
     */
    private final Cache<String, AipOcr> clientCache = CacheBuilder.newBuilder()
            .maximumSize(50)
            .expireAfterAccess(1, TimeUnit.HOURS)
            .removalListener((RemovalNotification<String, AipOcr> notification) -> {
                log.info("OCR客户端缓存清理: credentialId={}, 原因={}",
                        notification.getKey(), notification.getCause());
            })
            .build();

    /**
     * 获取OCR客户端（带缓存）
     *
     * @param credentialId 凭据ID
     * @param credential   OCR凭据
     * @return AipOcr客户端实例
     */
    public AipOcr getOrCreateClient(String credentialId, OcrCredential credential) {
        try {
            return clientCache.get(credentialId, () -> {
                log.info("创建新的OCR客户端实例: credentialId={}", credentialId);
                AipOcr client = new AipOcr(
                        credential.getAppId(),
                        credential.getApiKey(),
                        credential.getSecretKey()
                );
                client.setConnectionTimeoutInMillis(credential.getConnectTimeout());
                client.setSocketTimeoutInMillis(credential.getReadTimeout());
                return client;
            });
        } catch (ExecutionException e) {
            log.error("获取OCR客户端失败: credentialId={}, error={}",
                    credentialId, e.getMessage(), e);
            throw new RuntimeException("获取OCR客户端失败: " + e.getMessage(), e);
        }
    }

    /**
     * 清除指定客户端缓存
     *
     * @param credentialId 凭据ID
     */
    public void invalidate(String credentialId) {
        clientCache.invalidate(credentialId);
        log.info("OCR客户端缓存已清除: credentialId={}", credentialId);
    }

    /**
     * 清除所有客户端缓存
     */
    public void invalidateAll() {
        clientCache.invalidateAll();
        log.info("所有OCR客户端缓存已清除");
    }

    /**
     * 获取缓存大小
     */
    public long getCacheSize() {
        return clientCache.size();
    }
}
