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
import cn.xjbpm.rule.handler.auth.DynamicUrlAuthenticator;
import cn.xjbpm.rule.repository.entity.DynamicUrlMappingEntity;
import cn.xjbpm.rule.repository.enums.AuthType;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @author 黄川 huchuc@vip.qq.com
 * Basic认证处理器
 * authConfig 配置格式: username:password
 */
@Slf4j
@Component
public class BasicAuthenticator implements DynamicUrlAuthenticator {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BASIC_PREFIX = "Basic ";

    @Override
    public AuthType getAuthType() {
        return AuthType.BASIC;
    }

    @Override
    public AuthResult authenticate(HttpServletRequest request, DynamicUrlMappingEntity mapping) {
        String authConfig = mapping.getAuthConfig();
        if (StrUtil.isBlank(authConfig)) {
            log.warn("Basic认证配置为空, mappingId: {}", mapping.getId());
            return AuthResult.fail("认证配置错误");
        }

        String[] parts = authConfig.split(":");
        if (parts.length != 2) {
            log.warn("Basic认证配置格式错误, mappingId: {}", mapping.getId());
            return AuthResult.fail("认证配置格式错误");
        }

        String expectedUsername = parts[0];
        String expectedPassword = parts[1];

        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (StrUtil.isBlank(authHeader) || !authHeader.startsWith(BASIC_PREFIX)) {
            return AuthResult.fail("缺少Basic认证信息");
        }

        try {
            String base64Credentials = authHeader.substring(BASIC_PREFIX.length());
            String credentials = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
            String[] credentialParts = credentials.split(":", 2);
            if (credentialParts.length != 2) {
                return AuthResult.fail("Basic认证格式错误");
            }

            String username = credentialParts[0];
            String password = credentialParts[1];

            if (expectedUsername.equals(username) && expectedPassword.equals(password)) {
                return AuthResult.success(username);
            }

            return AuthResult.fail("用户名或密码错误");
        } catch (Exception e) {
            log.error("Basic认证解析异常", e);
            return AuthResult.fail("认证解析失败");
        }
    }
}
