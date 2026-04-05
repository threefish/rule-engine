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

import cn.xjbpm.rule.engine.runtime.RuleFlowExcuteService;
import cn.xjbpm.rule.handler.DynamicUrlHandler;
import cn.xjbpm.rule.handler.auth.DynamicUrlAuthenticatorManager;
import cn.xjbpm.rule.repository.entity.DynamicUrlMappingEntity;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 黄川 huchuc@vip.qq.com
 * 动态URL映射管理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicUrlMappingManager {

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;
    private final RuleFlowExcuteService ruleFlowExcuteService;
    private final DynamicUrlAuthenticatorManager authenticatorManager;
    private final Map<String, RequestMappingInfo> registeredMappings = new ConcurrentHashMap<>();
    private final Map<String, DynamicUrlMappingEntity> mappingCache = new ConcurrentHashMap<>();
    private DynamicUrlHandler dynamicUrlHandler;

    @PostConstruct
    public void init() {
        this.dynamicUrlHandler = new DynamicUrlHandler(ruleFlowExcuteService, mappingCache, authenticatorManager);
    }

    public void registerMapping(DynamicUrlMappingEntity mapping) {
        String mappingKey = buildMappingKey(mapping.getUrlPath(), mapping.getHttpMethod());

        if (registeredMappings.containsKey(mappingKey)) {
            log.warn("URL映射已存在，将先卸载: {}", mappingKey);
            unregisterMapping(mapping.getUrlPath(), mapping.getHttpMethod());
        }

        try {
            RequestMappingInfo.Builder builder = RequestMappingInfo
                    .paths(mapping.getUrlPath())
                    .methods(RequestMethod.valueOf(mapping.getHttpMethod()));
            RequestMappingInfo mappingInfo = builder.build();
            Method handleMethod = DynamicUrlHandler.class.getMethod("handle", HttpServletRequest.class);
            requestMappingHandlerMapping.registerMapping(mappingInfo, dynamicUrlHandler, handleMethod);
            registeredMappings.put(mappingKey, mappingInfo);
            mappingCache.put(mappingKey, mapping);

            log.info("成功注册动态URL映射: {} {}", mapping.getHttpMethod(), mapping.getUrlPath());
        } catch (NoSuchMethodException e) {
            log.error("注册动态URL映射失败: {} {}", mapping.getHttpMethod(), mapping.getUrlPath(), e);
            throw new RuntimeException("注册动态URL映射失败", e);
        }
    }

    public void unregisterMapping(String urlPath, String httpMethod) {
        String mappingKey = buildMappingKey(urlPath, httpMethod);

        RequestMappingInfo mappingInfo = registeredMappings.remove(mappingKey);
        mappingCache.remove(mappingKey);

        if (mappingInfo != null) {
            requestMappingHandlerMapping.unregisterMapping(mappingInfo);
            log.info("成功卸载动态URL映射: {} {}", httpMethod, urlPath);
        } else {
            log.warn("未找到要卸载的URL映射: {} {}", httpMethod, urlPath);
        }
    }

    public boolean isMappingRegistered(String urlPath, String httpMethod) {
        String mappingKey = buildMappingKey(urlPath, httpMethod);
        return registeredMappings.containsKey(mappingKey);
    }

    public int getRegisteredMappingCount() {
        return registeredMappings.size();
    }

    private String buildMappingKey(String urlPath, String httpMethod) {
        return httpMethod.toUpperCase() + ":" + urlPath;
    }

}
