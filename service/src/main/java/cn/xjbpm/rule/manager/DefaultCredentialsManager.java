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
import cn.xjbpm.rule.engine.runtime.model.credentials.*;
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
import java.util.function.Supplier;

/**
 * 默认凭据管理器实现
 *
 * @author 黄川 huchuc@vip.qq.com
 * date: 2025/12/18
 */
@Service
@AllArgsConstructor
public class DefaultCredentialsManager implements CredentialsManager {

    private final CredentialsService credentialsService;
    private final DataBaseManager dataBaseManager;
    private final OcrClientManager ocrClientManager;
    private final RocketMQClientManager rocketMQClientManager;
    private final WxPayClientManager wxPayClientManager;

    @Override
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
                        httpCredentials.getHeaders().put(authData.get("key"), authData.get("value"));
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
    public ApikeyCredential getApikeyCredential(String credentialId) {
        if (StringUtils.isNotBlank(credentialId)) {
            CredentialsVO.Details details = credentialsService.details(Long.valueOf(credentialId));
            if (Objects.nonNull(details)) {
                Map<String, String> authData = JsonUtils.json2Obj(details.getAuthData(), Map.class);
                return ApikeyCredential.builder()
                        .type(details.getType().toString())
                        .apiKey(authData.get("apiKey"))
                        .build();
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
    public OcrCredential getOcrCredential(String credentialId) {
        OcrCredential ocrCredential = getCredential(credentialId, OcrCredential::new, OcrCredential.class);
        if (StringUtils.isNotBlank(credentialId) && ocrCredential != null) {
            ocrCredential.setClient(ocrClientManager.getOrCreateClient(credentialId, ocrCredential));
        }
        return ocrCredential;
    }

    @Override
    public SshCredential getSshCredential(String credentialId) {
        return getCredential(credentialId, SshCredential::new, SshCredential.class);
    }

    @Override
    public FtpCredential getFtpCredential(String credentialId) {
        return getCredential(credentialId, FtpCredential::new, FtpCredential.class);
    }

    @Override
    public EmailCredential getEmailCredential(String credentialId) {
        return getCredential(credentialId, EmailCredential::new, EmailCredential.class);
    }

    @Override
    public RedisCredential getRedisCredential(String credentialId) {
        return getCredential(credentialId, RedisCredential::new, RedisCredential.class);
    }

    @Override
    public RabbitMQCredential getRabbitMQCredential(String credentialId) {
        return getCredential(credentialId, RabbitMQCredential::new, RabbitMQCredential.class);
    }

    @Override
    public KafkaCredential getKafkaCredential(String credentialId) {
        return getCredential(credentialId, KafkaCredential::new, KafkaCredential.class);
    }

    @Override
    public MQTTCredential getMQTTCredential(String credentialId) {
        return getCredential(credentialId, MQTTCredential::new, MQTTCredential.class);
    }

    @Override
    public DingtalkCredential getDingtalkCredential(String credentialId) {
        return getCredential(credentialId, DingtalkCredential::new, DingtalkCredential.class);
    }

    @Override
    public FeishuCredential getFeishuCredential(String credentialId) {
        return getCredential(credentialId, FeishuCredential::new, FeishuCredential.class);
    }

    @Override
    public CryptoCredential getCryptoCredential(String credentialId) {
        return getCredential(credentialId, CryptoCredential::new, CryptoCredential.class);
    }

    @Override
    public RocketMQCredential getRocketMQCredential(String credentialId) {
        RocketMQCredential rocketMQCredential = getCredential(credentialId, RocketMQCredential::new, RocketMQCredential.class);
        if (StringUtils.isNotBlank(credentialId) && rocketMQCredential != null) {
            rocketMQCredential.setProducer(rocketMQClientManager.getOrCreateProducer(credentialId, rocketMQCredential));
        }
        return rocketMQCredential;
    }

    @Override
    public WxPayCredential getWxPayCredential(String credentialId) {
        WxPayCredential wxPayCredential = getCredential(credentialId, WxPayCredential::new, WxPayCredential.class);
        if (StringUtils.isNotBlank(credentialId) && wxPayCredential != null) {
            wxPayClientManager.initClient(credentialId, wxPayCredential);
        }
        return wxPayCredential;
    }

    @Override
    public GitCredential getGitCredential(String credentialId) {
        return getCredential(credentialId, GitCredential::new, GitCredential.class);
    }


    /**
     * 通用凭据获取方法
     *
     * @param credentialId    凭据ID
     * @param defaultSupplier 默认实例提供者
     * @param credentialClass 凭据类型
     * @param <T>             凭据类型
     * @return 凭据实例
     */
    private <T> T getCredential(String credentialId, Supplier<T> defaultSupplier, Class<T> credentialClass) {
        T credential = defaultSupplier.get();
        if (StringUtils.isNotBlank(credentialId)) {
            CredentialsVO.Details details = credentialsService.details(Long.valueOf(credentialId));
            if (Objects.nonNull(details)) {
                credential = JsonUtils.json2Obj(details.getAuthData(), credentialClass);
            }
        }
        return credential;
    }
}
