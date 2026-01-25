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

import cn.hutool.core.util.IdUtil;
import cn.xjbpm.rule.listener.event.DisabledApiKeyEvent;
import cn.xjbpm.rule.repository.AuthoriztionRepository;
import cn.xjbpm.rule.repository.entity.AuthoriztionEntity;
import cn.xjbpm.rule.vo.AuthoriztionVO;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2025/11/27
 */
@Service
@AllArgsConstructor
public class AuthoriztionService {

    private final AuthoriztionRepository authoriztionRepository;
    private final ApplicationEventPublisher applicationEventPublisher;


    public Page<AuthoriztionEntity> findPage(String key, Pageable pageable) {
        if (StringUtils.hasText(key)) {
            return authoriztionRepository.findAllByApiKey(key, pageable);
        }
        return authoriztionRepository.findAll(pageable);
    }

    public boolean save(AuthoriztionVO.SaveRequest request) {
        AuthoriztionEntity entity = null;
        if (Objects.nonNull(request.getId())) {
            entity = authoriztionRepository.findById(request.getId()).orElse(null);
        }
        if (Objects.isNull(entity)) {
            entity = new AuthoriztionEntity();
            entity.setApiKey(IdUtil.fastSimpleUUID());
            entity.setEnabled(true);
        }
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setAuthoriztion(request.getAuthoriztion());
        authoriztionRepository.save(entity);
        return true;
    }


    @Transactional(rollbackFor = Exception.class)
    public boolean changeStatus(Long id, boolean enabled) {
        AuthoriztionEntity entity = authoriztionRepository.findById(id).orElse(null);
        entity.setEnabled(enabled);
        authoriztionRepository.save(entity);
        if (enabled == false) {
            applicationEventPublisher.publishEvent(DisabledApiKeyEvent.create(entity.getApiKey()));
        }
        return true;
    }

    public AuthoriztionEntity findByApiKey(String apiKey) {
        return authoriztionRepository.findByApiKey(apiKey).orElse(null);
    }
}
