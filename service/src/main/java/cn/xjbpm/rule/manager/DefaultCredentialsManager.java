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

import cn.xjbpm.rule.common.utils.JsonUtils;
import cn.xjbpm.rule.common.utils.StringUtils;
import cn.xjbpm.rule.custom.CredentialsManager;
import cn.xjbpm.rule.engine.runtime.model.credentials.ApikeyCredential;
import cn.xjbpm.rule.engine.runtime.model.credentials.HttpCredential;
import cn.xjbpm.rule.engine.runtime.model.credentials.SshCredential;
import cn.xjbpm.rule.repository.enums.CredentialsType;
import cn.xjbpm.rule.service.CredentialsService;
import cn.xjbpm.rule.vo.CredentialsVO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2025/12/18
 */
@Service
@AllArgsConstructor
public class DefaultCredentialsManager implements CredentialsManager {

    private final CredentialsService credentialsService;

    public HttpCredential getHttpCredential(String credentialId) {
        HttpCredential httpCredentials = new HttpCredential();
        if (StringUtils.isNotBlank(credentialId)) {
            CredentialsVO.Details details = credentialsService.details(Long.valueOf(credentialId));
            if (Objects.nonNull(details)) {
                Map<String, String> authData = JsonUtils.json2Obj(details.getAuthData(), Map.class);
                httpCredentials.setHeaders(new HashMap<>());
                CredentialsType type = details.getType();
                switch (type) {
                    case bearer_auth:
                        httpCredentials.getHeaders().put("Authorization", "Bearer " + authData.get("token"));
                        break;
                    case header_auth:
                        String name = authData.get("key");
                        String value = authData.get("value");
                        httpCredentials.getHeaders().put(name, value);
                        break;
                    case basic_auth:
                        String username = authData.get("username");
                        String password = authData.get("password");
                        httpCredentials.getHeaders().put("Authorization", "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes()));
                        break;
                    default:
                        break;
                }
            }
        }
        return httpCredentials;
    }

    @Override
    public SshCredential getSshCredential(String credentialId) {
        SshCredential sshCredential = new SshCredential();
        if (StringUtils.isNotBlank(credentialId)) {
            CredentialsVO.Details details = credentialsService.details(Long.valueOf(credentialId));
            if (Objects.nonNull(details)) {
                sshCredential = JsonUtils.json2Obj(details.getAuthData(), SshCredential.class);
            }
        }
        return sshCredential;
    }

    @Override
    public ApikeyCredential getApikeyCredential(String credentialId) {
        if (StringUtils.isNotBlank(credentialId)) {
            CredentialsVO.Details details = credentialsService.details(Long.valueOf(credentialId));
            if (Objects.nonNull(details)) {
                Map<String, String> authData = JsonUtils.json2Obj(details.getAuthData(), Map.class);
                String apiKey = authData.get("apiKey");
                String type = details.getType().toString();
                return ApikeyCredential.builder().type(type).apiKey(apiKey).build();
            }
        }
        return null;
    }
}