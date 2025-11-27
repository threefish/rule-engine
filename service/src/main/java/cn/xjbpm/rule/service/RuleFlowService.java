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

import cn.xjbpm.rule.repository.RuleFlowRepository;
import cn.xjbpm.rule.repository.entity.RuleFlowEntity;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.NoSuchElementException;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2025/11/22
 */
@Service
@AllArgsConstructor
public class RuleFlowService {

    private final RuleFlowRepository ruleFlowRepository;


    /**
     * 根据ID获取规则流程实体的引用。
     *
     * @param id 规则流程的ID
     * @return 匹配的规则流程实体的引用
     * @throws NoSuchElementException 如果指定的ID不存在
     */
    public RuleFlowEntity findById(Long id) {
        return ruleFlowRepository.findById(id).orElse(null);
    }

    /**
     * 根据唯一标识 key 查找单个规则流程实体。
     * * @param key 规则流程的唯一标识
     *
     * @return 匹配的 RuleFlowEntity，如果不存在则返回 null
     */
    public RuleFlowEntity findByKey(String key) {
        return ruleFlowRepository.findByKey(key).orElse(null);
    }

    /**
     * 根据唯一标识 key 查找版本最高的规则流程实体。
     *
     * @param key 规则流程的唯一标识
     * @return 匹配版本最高的 RuleFlowEntity，如果不存在则返回 null
     */
    public RuleFlowEntity findByKeyAndMaxVerison(String key) {
        return ruleFlowRepository.findFirstByKeyOrderByVersionDesc(key).orElse(null);
    }

    public RuleFlowEntity findByKeyAndVerison(String key, Integer version) {
        return ruleFlowRepository.findByKeyAndVersion(key, version).orElse(null);
    }


    /**
     * 分页查询所有规则流程实体。
     *
     * @param pageable 分页信息 (页码、大小、排序等)
     * @param key
     * @param name
     * @return 规则流程实体的分页结果
     */
    public Page<RuleFlowEntity> findPage(Pageable pageable, String key, String name) {
            final boolean hasKey = StringUtils.hasText(key);
            final boolean hasName = StringUtils.hasText(name);
            if (hasKey && hasName) {
                return ruleFlowRepository.findByKeyAndNameContaining(key, name, pageable);
            } else if (hasKey) {
                return ruleFlowRepository.findByKey(key, pageable);
            } else if (hasName) {
                return ruleFlowRepository.findByNameContaining(name, pageable);
            } else {
                return ruleFlowRepository.findAll(pageable);
            }
        }

    /**
     * 保存或更新规则流程实体，严格限制 Key 字段在更新时不可修改。
     *
     * @param entity 待保存或更新的 RuleFlowEntity
     * @return 保存后的实体对象
     * @throws IllegalArgumentException 如果 Key 字段在更新时被修改或实体/Key为空
     * @throws NoSuchElementException   如果尝试更新但实体ID不存在
     */
    @Transactional(rollbackFor = Exception.class)
    public RuleFlowEntity saveOrUpdate(RuleFlowEntity entity) {
        if (entity == null || entity.getKey() == null) {
            throw new IllegalArgumentException("RuleFlow entity and its key must not be null.");
        }
        if (entity.getId() == null) {
            entity.setVersion(0);
            return ruleFlowRepository.save(entity);
        } else {
            RuleFlowEntity existing = ruleFlowRepository.findById(entity.getId())
                    .orElseThrow(() -> new NoSuchElementException("Cannot update: RuleFlow with ID " + entity.getId() + " not found."));
            if (!existing.getKey().equals(entity.getKey())) {
                throw new IllegalArgumentException("RuleFlow key is immutable and cannot be changed from '" + existing.getKey() + "' to '" + entity.getKey() + "'.");
            }
            existing.setName(entity.getName());
            existing.setDescription(entity.getDescription());
            existing.setContent(entity.getContent());
            return ruleFlowRepository.save(existing);
        }
    }
}