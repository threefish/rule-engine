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
package cn.xjbpm.rule.handler.auth;

import cn.xjbpm.rule.repository.enums.AuthType;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 黄川 huchuc@vip.qq.com
 * 动态URL认证处理器管理器
 * 支持运行时动态注册和获取认证处理器
 */
@Slf4j
@Component
public class DynamicUrlAuthenticatorManager {

    private final Map<AuthType, DynamicUrlAuthenticator> authenticatorMap = new ConcurrentHashMap<>();

    @Autowired(required = false)
    private List<DynamicUrlAuthenticator> authenticators;

    @PostConstruct
    public void init() {
        if (authenticators != null) {
            authenticators.forEach(this::registerAuthenticator);
        }
        log.info("DynamicUrlAuthenticatorManager 初始化完成, 已注册 {} 个认证处理器", authenticatorMap.size());
    }

    /**
     * 注册认证处理器
     */
    public void registerAuthenticator(DynamicUrlAuthenticator authenticator) {
        AuthType authType = authenticator.getAuthType();
        authenticatorMap.put(authType, authenticator);
        log.info("注册认证处理器: {} -> {}", authType, authenticator.getClass().getSimpleName());
    }

    /**
     * 获取认证处理器
     */
    public DynamicUrlAuthenticator getAuthenticator(AuthType authType) {
        return authenticatorMap.get(authType);
    }

    /**
     * 是否存在指定类型的认证处理器
     */
    public boolean hasAuthenticator(AuthType authType) {
        return authenticatorMap.containsKey(authType);
    }
}
