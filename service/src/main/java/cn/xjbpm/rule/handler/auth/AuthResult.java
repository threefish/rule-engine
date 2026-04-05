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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 认证结果
 * @author 黄川 huchuc@vip.qq.com
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResult {
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