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
import cn.xjbpm.rule.repository.RuleFlowAuthoriztionRepository;
import cn.xjbpm.rule.repository.entity.RuleFlowAuthoriztionEntity;
import cn.xjbpm.rule.vo.RuleFlowAuthoriztionVO;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2025/11/27
 */
@Service
@AllArgsConstructor
public class RuleFlowAuthoriztionService {

    private final RuleFlowAuthoriztionRepository ruleFlowAuthoriztionRepository;


    public Page<RuleFlowAuthoriztionEntity> findPage(String key, Pageable pageable) {
        if (StringUtils.hasText(key)) {
            return ruleFlowAuthoriztionRepository.findAllByAppCode(key, pageable);
        }
        return ruleFlowAuthoriztionRepository.findAll(pageable);
    }

    public RuleFlowAuthoriztionVO.AddResponse save(RuleFlowAuthoriztionVO.SaveRequest request) {
        RuleFlowAuthoriztionEntity entity = ruleFlowAuthoriztionRepository.findByAppCode(request.getAppCode()).orElse(null);
        RuleFlowAuthoriztionVO.AddResponse response = new RuleFlowAuthoriztionVO.AddResponse();
        if (Objects.isNull(entity)) {
            entity = new RuleFlowAuthoriztionEntity();
            entity.setAppCode(request.getAppCode());
            // 此处为了简化，默认使用随机数来作为密钥（你可以自行采用其他方式）
            entity.setSecretKey(IdUtil.fastSimpleUUID());
            entity.setEnabled(true);
            response.setAppCode(entity.getAppCode());
            response.setSecretKey(entity.getSecretKey());
        }
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setAuthoriztion(request.getAuthoriztion());
        ruleFlowAuthoriztionRepository.save(entity);
        return response;
    }

    public RuleFlowAuthoriztionVO.AddResponse resetKey(Long id) {
        RuleFlowAuthoriztionEntity entity = ruleFlowAuthoriztionRepository.findById(id).orElse(null);
        entity.setSecretKey(IdUtil.fastSimpleUUID());
        ruleFlowAuthoriztionRepository.save(entity);
        RuleFlowAuthoriztionVO.AddResponse response = new RuleFlowAuthoriztionVO.AddResponse();
        response.setAppCode(entity.getAppCode());
        response.setSecretKey(entity.getSecretKey());
        return response;
    }

    public boolean changeStatus(Long id, boolean enabled) {
        RuleFlowAuthoriztionEntity entity = ruleFlowAuthoriztionRepository.findById(id).orElse(null);
        entity.setEnabled(enabled);
        ruleFlowAuthoriztionRepository.save(entity);
        return true;
    }

    public RuleFlowAuthoriztionEntity findByAppCode(String appCode) {
        return ruleFlowAuthoriztionRepository.findByAppCode(appCode).orElse(null);
    }
}
