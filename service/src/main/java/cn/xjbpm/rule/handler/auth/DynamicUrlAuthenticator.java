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

import cn.xjbpm.rule.repository.entity.DynamicUrlMappingEntity;
import cn.xjbpm.rule.repository.enums.AuthType;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 黄川 huchuc@vip.qq.com
 * 动态URL认证处理器接口
 */
public interface DynamicUrlAuthenticator {

    /**
     * 获取支持的认证类型
     */
    AuthType getAuthType();

    /**
     * 执行认证
     *
     * @param request HTTP请求
     * @param mapping URL映射配置
     * @return 认证结果
     */
    AuthResult authenticate(HttpServletRequest request, DynamicUrlMappingEntity mapping);

    /**
     * 认证结果
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class AuthResult {
        private boolean success;
        private String errorMessage;
        private Object principal;

        public static AuthResult success() {
            return new AuthResult(true, null, null);
        }

        public static AuthResult success(Object principal) {
            return new AuthResult(true, null, principal);
        }

        public static AuthResult fail(String errorMessage) {
            return new AuthResult(false, errorMessage, null);
        }
    }
}
