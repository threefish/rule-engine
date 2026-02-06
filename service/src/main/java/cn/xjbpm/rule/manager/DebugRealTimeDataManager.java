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

import cn.xjbpm.rule.dto.ExcuteRuleFlowResult;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author 黄川 huchuc@vip.qq.com
 */
@Component
@Slf4j
public class DebugRealTimeDataManager {

    /**
     * 缓存容器
     * expireAfterWrite: 最后一次写入后5分钟过期
     * maximumSize: 最大缓存100条，超过后基于 LRU (最近最少使用) 策略自动剔除
     */
    private static final Cache<Long, ExcuteRuleFlowResult> dataCache = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(100)
            .removalListener(notification -> {
                log.debug("Debug数据清理 - ID: {}, 原因: {}", notification.getKey(), notification.getCause());
            })
            .build();

    /**
     * 获取缓存数据
     *
     * @param id 规则流执行ID
     * @return 执行结果
     */
    public ExcuteRuleFlowResult get(Long id) {
        if (id == null) {
            return null;
        }
        return dataCache.getIfPresent(id);
    }

    /**
     * 存入或更新缓存数据
     *
     * @param id   规则流执行ID
     * @param data 执行结果
     */
    public void put(Long id, ExcuteRuleFlowResult data) {
        if (id == null || data == null) {
            return;
        }
        dataCache.put(id, data);
    }

    /**
     * 手动清除某个缓存
     */
    public void remove(Long id) {
        if (id != null) {
            dataCache.invalidate(id);
        }
    }

    /**
     * 获取当前缓存中的大约数量
     */
    public long size() {
        return dataCache.size();
    }

}