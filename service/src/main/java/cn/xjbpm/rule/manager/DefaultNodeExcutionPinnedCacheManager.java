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

import cn.xjbpm.rule.custom.NodeExcutionPinnedCacheManager;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author 黄川 huchuc@vip.qq.com
 */
@Slf4j
@Service
public class DefaultNodeExcutionPinnedCacheManager implements NodeExcutionPinnedCacheManager {

    private final Cache<String, Map<String, Map<String, Object>>> cache = CacheBuilder.newBuilder()
            .maximumSize(500)
            .expireAfterAccess(1, TimeUnit.DAYS)
            .build();

    @Override
    public Map<String, Object> getNodeLatestPinnedResult(String ruleFlowKey, String nodeId) {
        if (ruleFlowKey == null || nodeId == null) {
            return null;
        }
        Map<String, Map<String, Object>> flowCache = cache.getIfPresent(ruleFlowKey);
        return (flowCache != null) ? flowCache.get(nodeId) : null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setNodeLatestPinnedResult(String ruleFlowKey, String id, Object object) {
        if (ruleFlowKey == null || id == null || object == null) {
            return;
        }
        try {
            Map<String, Map<String, Object>> flowCache = cache.get(ruleFlowKey, ConcurrentHashMap::new);
            if (object instanceof Map) {
                flowCache.put(id, (Map<String, Object>) object);
            }
        } catch (ExecutionException e) {
            log.error("Failed to load cache for ruleFlowKey: {}", ruleFlowKey, e);
        }
    }
}
