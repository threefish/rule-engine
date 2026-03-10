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
import cn.xjbpm.rule.engine.runtime.model.credentials.DataBaseCredential;
import cn.xjbpm.rule.engine.runtime.model.credentials.EmailCredential;
import cn.xjbpm.rule.engine.runtime.model.credentials.FtpCredential;
import cn.xjbpm.rule.engine.runtime.model.credentials.HttpCredential;
import cn.xjbpm.rule.engine.runtime.model.credentials.KafkaCredential;
import cn.xjbpm.rule.engine.runtime.model.credentials.MQTTCredential;
import cn.xjbpm.rule.engine.runtime.model.credentials.RabbitMQCredential;
import cn.xjbpm.rule.engine.runtime.model.credentials.RedisCredential;
import cn.xjbpm.rule.engine.runtime.model.credentials.SshCredential;
import cn.xjbpm.rule.repository.enums.CredentialsType;
import cn.xjbpm.rule.service.CredentialsService;
import cn.xjbpm.rule.vo.CredentialsVO;
import lombok.AllArgsConstructor;
import org.nutz.dao.Dao;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

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
    private final DataBaseManager dataBaseManager;

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

    @Override
    public DataBaseCredential getDataBaseCredential(String credentialId) {
        CredentialsVO.Details details = credentialsService.details(Long.valueOf(credentialId));
        Assert.notNull(details, "credential is null");
        Map<String, String> authData = JsonUtils.json2Obj(details.getAuthData(), Map.class);
        Dao dao = dataBaseManager.getOrCreateDao(authData);
        return DataBaseCredential.builder().dao(dao).build();
    }

    @Override
    public FtpCredential getFtpCredential(String credentialId) {
        FtpCredential ftpCredential = new FtpCredential();
        if (StringUtils.isNotBlank(credentialId)) {
            CredentialsVO.Details details = credentialsService.details(Long.valueOf(credentialId));
            if (Objects.nonNull(details)) {
                ftpCredential = JsonUtils.json2Obj(details.getAuthData(), FtpCredential.class);
            }
        }
        return ftpCredential;
    }

    @Override
    public EmailCredential getEmailCredential(String credentialId) {
        EmailCredential emailCredential = new EmailCredential();
        if (StringUtils.isNotBlank(credentialId)) {
            CredentialsVO.Details details = credentialsService.details(Long.valueOf(credentialId));
            if (Objects.nonNull(details)) {
                emailCredential = JsonUtils.json2Obj(details.getAuthData(), EmailCredential.class);
            }
        }
        return emailCredential;
    }

    @Override
    public RedisCredential getRedisCredential(String credentialId) {
        RedisCredential redisCredential = new RedisCredential();
        if (StringUtils.isNotBlank(credentialId)) {
            CredentialsVO.Details details = credentialsService.details(Long.valueOf(credentialId));
            if (Objects.nonNull(details)) {
                redisCredential = JsonUtils.json2Obj(details.getAuthData(), RedisCredential.class);
            }
        }
        return redisCredential;
    }

    @Override
    public RabbitMQCredential getRabbitMQCredential(String credentialId) {
        RabbitMQCredential rabbitMQCredential = new RabbitMQCredential();
        if (StringUtils.isNotBlank(credentialId)) {
            CredentialsVO.Details details = credentialsService.details(Long.valueOf(credentialId));
            if (Objects.nonNull(details)) {
                rabbitMQCredential = JsonUtils.json2Obj(details.getAuthData(), RabbitMQCredential.class);
            }
        }
        return rabbitMQCredential;
    }

    @Override
    public KafkaCredential getKafkaCredential(String credentialId) {
        KafkaCredential kafkaCredential = new KafkaCredential();
        if (StringUtils.isNotBlank(credentialId)) {
            CredentialsVO.Details details = credentialsService.details(Long.valueOf(credentialId));
            if (Objects.nonNull(details)) {
                kafkaCredential = JsonUtils.json2Obj(details.getAuthData(), KafkaCredential.class);
            }
        }
        return kafkaCredential;
    }

    @Override
    public MQTTCredential getMQTTCredential(String credentialId) {
        MQTTCredential mqttCredential = new MQTTCredential();
        if (StringUtils.isNotBlank(credentialId)) {
            CredentialsVO.Details details = credentialsService.details(Long.valueOf(credentialId));
            if (Objects.nonNull(details)) {
                mqttCredential = JsonUtils.json2Obj(details.getAuthData(), MQTTCredential.class);
            }
        }
        return mqttCredential;
    }


}