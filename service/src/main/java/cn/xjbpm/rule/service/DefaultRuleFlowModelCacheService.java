/*
 * Copyright 2025 threefish.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.xjbpm.rule.service;

import cn.xjbpm.rule.custom.RuleFlowModelCacheService;
import cn.xjbpm.rule.engine.definition.model.RuleFlowModel;
import cn.xjbpm.rule.vo.RuleFlowVO;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2025/11/22
 * 规则流缓存服务（集群部署时请使用分布式缓存）
 */
@Service
@AllArgsConstructor
public class DefaultRuleFlowModelCacheService implements RuleFlowModelCacheService {

    private final RuleFlowService ruleFlowService;

    private final Cache<String, RuleFlowModel> cache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build();

    @Override
    public RuleFlowModel getModel(String key) {
        try {
            return cache.get(key, () -> {
                RuleFlowVO entity = ruleFlowService.findByKeyAndDeployed(key);
                if (Objects.isNull(entity)) {
                    throw new RuntimeException("未找到规则流：" + key);
                }
                RuleFlowModel processModel = convertToModel(entity.getContent());
                processModel.setKey(entity.getKey());
                processModel.setName(entity.getName());
                processModel.setDescription(entity.getDescription());
                return processModel;
            });
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new RuntimeException("缓存加载失败: " + e.getMessage(), e);
        }
    }

    @Override
    public RuleFlowModel convertToModel(String content) {
        return RuleFlowModelCacheService.super.convertToModel(content);
    }

    /**
     * 删除缓存
     *
     * @param key
     */
    public void removeCache(String key) {
        cache.invalidate(key);
    }
}