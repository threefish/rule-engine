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
package cn.xjbpm.rule.handler.auth.impl;

import cn.hutool.core.util.StrUtil;
import cn.xjbpm.rule.engine.aviator.AviatorContext;
import cn.xjbpm.rule.engine.aviator.AviatorExecutor;
import cn.xjbpm.rule.handler.auth.DynamicUrlAuthenticator;
import cn.xjbpm.rule.repository.entity.DynamicUrlMappingEntity;
import cn.xjbpm.rule.repository.enums.AuthType;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * 自定义脚本认证处理器
 * authConfig 配置格式: Aviator脚本表达式，返回true表示认证通过
 * 脚本上下文变量:
 * - request: HttpServletRequest对象
 * - headers: 请求头Map
 * - parameters: 请求参数Map
 */
@Slf4j
@Component
public class CustomScriptAuthenticator implements DynamicUrlAuthenticator {

    @Override
    public AuthType getAuthType() {
        return AuthType.CUSTOM;
    }

    @Override
    public AuthResult authenticate(HttpServletRequest request, DynamicUrlMappingEntity mapping) {
        String script = mapping.getAuthConfig();
        if (StrUtil.isBlank(script)) {
            log.warn("自定义脚本认证配置为空, mappingId: {}", mapping.getId());
            return AuthResult.fail("认证配置错误");
        }

        try {
            Map<String, Object> context = buildScriptContext(request);
            AviatorContext aviatorContext = AviatorContext.create(script, context);
            Object result = AviatorExecutor.execute(aviatorContext);
            
            if (result instanceof Boolean && (Boolean) result) {
                return AuthResult.success("script-auth");
            }
            
            return AuthResult.fail("认证失败");
        } catch (Exception e) {
            log.error("自定义脚本认证执行异常, mappingId: {}, script: {}", mapping.getId(), script, e);
            return AuthResult.fail("认证执行异常: " + e.getMessage());
        }
    }

    private Map<String, Object> buildScriptContext(HttpServletRequest request) {
        Map<String, Object> context = new HashMap<>();
        context.put("request", request);
        
        Map<String, Object> headers = new HashMap<>();
        var headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            headers.put(name.toLowerCase(), request.getHeader(name));
        }
        context.put("headers", headers);
        
        Map<String, Object> parameters = new HashMap<>();
        var parameterMap = request.getParameterMap();
        for (var entry : parameterMap.entrySet()) {
            String[] values = entry.getValue();
            parameters.put(entry.getKey(), values != null && values.length == 1 ? values[0] : values);
        }
        context.put("parameters", parameters);
        
        return context;
    }
}
