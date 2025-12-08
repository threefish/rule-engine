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

import cn.xjbpm.rule.repository.RuleFlowExcuteLogRepository;
import cn.xjbpm.rule.repository.entity.RuleFlowExcuteLogEntity;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2025/11/27
 */
@Service
@AllArgsConstructor
public class RuleFlowExcuteLogService {

    private final RuleFlowExcuteLogRepository ruleFlowExcuteLogRepository;

    /**
     * 保存执行日志
     *
     * @param entity
     */
    public void save(RuleFlowExcuteLogEntity entity) {
        ruleFlowExcuteLogRepository.save(entity);

    }

    public Page<RuleFlowExcuteLogEntity> findPage(Pageable pageable, String key, String requestId) {
        if (StringUtils.hasText(requestId)) {
            return ruleFlowExcuteLogRepository.findAllByRequestId(requestId, pageable);
        }
        if (StringUtils.hasText(key)) {
            return ruleFlowExcuteLogRepository.findAllByRuleFlowKey(key, pageable);
        }
        return new PageImpl<>(Collections.emptyList());
    }

    public RuleFlowExcuteLogEntity findById(Long id) {
        return ruleFlowExcuteLogRepository.findById(id).orElse(null);
    }

    public RuleFlowExcuteLogEntity findByRequestId(String id) {
        return ruleFlowExcuteLogRepository.findByRequestId(id).orElse(null);
    }
}
