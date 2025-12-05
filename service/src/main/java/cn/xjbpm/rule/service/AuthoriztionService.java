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

import cn.xjbpm.rule.common.utils.JsonUtils;
import cn.xjbpm.rule.repository.entity.RuleFlowAuthoriztionEntity;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2025/12/5
 */
@Service
@AllArgsConstructor
public class AuthoriztionService {


    private final static Cache<String, TokenAuth> cache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build();


    private final RuleFlowAuthoriztionService ruleFlowAuthoriztionService;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    /**
     * Token 验证
     */
    public boolean validateToken(String appCode, String token) {
        TokenAuth tokenAuth = getTokenAuth(appCode);
        if (Objects.nonNull(tokenAuth)) {
            return Objects.equals(token, tokenAuth.getSecretKey());
        }
        return false;
    }

    private TokenAuth getTokenAuth(String key) {
        try {
            return cache.get(key, () -> {
                RuleFlowAuthoriztionEntity entity = ruleFlowAuthoriztionService.findByAppCode(key);
                if (Objects.nonNull(entity)) {
                    TokenAuth tokenAuth = new TokenAuth();
                    tokenAuth.setSecretKey(entity.getSecretKey());
                    tokenAuth.setRules(JsonUtils.json2List(entity.getAuthoriztion(), String.class));
                    return tokenAuth;
                }
                return null;
            });
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new RuntimeException("缓存加载失败: " + e.getMessage(), e);
        }
    }

    /**
     * 验证规则
     *
     * @param appCode
     * @param ruleFlowKey
     * @return
     */

    public boolean validateRuleFlowKey(String appCode, String ruleFlowKey) {
        TokenAuth tokenAuth = getTokenAuth(appCode);
        for (String rule : tokenAuth.getRules()) {
            if (antPathMatcher.match(rule, ruleFlowKey)) {
                return true;
            }
        }
        return true;
    }

    @Data
    public class TokenAuth {
        private String secretKey;
        private List<String> rules;
    }

}
