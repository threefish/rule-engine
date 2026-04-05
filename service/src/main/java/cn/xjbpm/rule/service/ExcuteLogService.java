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

package cn.xjbpm.rule.service;

import cn.xjbpm.rule.repository.ExcuteLogRepository;
import cn.xjbpm.rule.repository.entity.ExcuteLogEntity;
import cn.xjbpm.rule.service.storage.StorageProvider;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Objects;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2025/11/27
 */
@Service
@AllArgsConstructor
public class ExcuteLogService {

    private final ExcuteLogRepository excuteLogRepository;

    private final StorageProvider storageProvider;

    /**
     * 保存执行日志
     *
     * @param entity
     */
    public void save(ExcuteLogEntity entity, String content) {
        String storePath = storageProvider.store(entity.getId().toString(), content);
        entity.setSnapshotPath(storePath);
        excuteLogRepository.save(entity);
    }

    public Page<ExcuteLogEntity> findPage(Pageable pageable, String key, String requestId) {
        if (StringUtils.hasText(requestId)) {
            return excuteLogRepository.findAllByRequestId(requestId, pageable);
        }
        if (StringUtils.hasText(key)) {
            return excuteLogRepository.findAllByRuleFlowKey(key, pageable);
        }
        return new PageImpl<>(Collections.emptyList());
    }

    public ExcuteLogEntity findById(Long id) {
        return process(excuteLogRepository.findById(id).orElse(null));
    }

    public ExcuteLogEntity findByRequestId(String id) {
        return process(excuteLogRepository.findByRequestId(id).orElse(null));
    }

    private ExcuteLogEntity process(ExcuteLogEntity entity) {
        if (Objects.nonNull(entity)) {
            entity.setContent(storageProvider.fetch(entity.getSnapshotPath()));
        }
        return entity;
    }
}