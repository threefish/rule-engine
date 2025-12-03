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

import cn.xjbpm.rule.listenner.event.RemoveRuleFlowCacheEvent;
import cn.xjbpm.rule.repository.RuleFlowRepository;
import cn.xjbpm.rule.repository.entity.RuleFlowEntity;
import cn.xjbpm.rule.repository.enums.RuleFlowStatus;
import cn.xjbpm.rule.utils.JpaUtil;
import cn.xjbpm.rule.utils.TransactionOptDelayerHolder;
import cn.xjbpm.rule.vo.RuleFlowVO;
import jakarta.persistence.criteria.Predicate;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2025/11/22
 */
@Service
@AllArgsConstructor
public class RuleFlowService {

    private final RuleFlowRepository ruleFlowRepository;
    private final ApplicationEventPublisher applicationEventPublisher;


    /**
     * 根据ID获取规则流程实体的引用。
     *
     * @param id 规则流程的ID
     * @return 匹配的规则流程实体的引用
     */
    public RuleFlowVO findById(Long id) {
        RuleFlowEntity entity = ruleFlowRepository.findById(id).orElse(null);
        return RuleFlowVO.create(entity);
    }


    /**
     * 根据Key获取规则流程实体的引用。
     *
     * @param key
     * @return
     */
    public RuleFlowVO findByKeyAndDeployed(String key) {
        RuleFlowEntity entity = ruleFlowRepository.findByKeyAndStatus(key, RuleFlowStatus.DEOPLOYED).orElse(null);
        return RuleFlowVO.create(entity);
    }


    /**
     * 分页查询所有规则流程实体。
     *
     * @param pageable 分页信息 (页码、大小、排序等)
     * @param key
     * @param name
     * @return 规则流程实体的分页结果
     */
    public Page<RuleFlowEntity> findPage(String key, String name, RuleFlowStatus status, Pageable pageable) {
        Specification<RuleFlowEntity> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(key)) {
                predicates.add(criteriaBuilder.equal(JpaUtil.field(root, RuleFlowEntity::getKey), key));
            }
            if (StringUtils.hasText(name)) {
                predicates.add(criteriaBuilder.like(JpaUtil.field(root, RuleFlowEntity::getName), "%" + name + "%"));
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(JpaUtil.field(root, RuleFlowEntity::getStatus), status));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        return ruleFlowRepository.findAll(specification, pageable);
    }


    /**
     * @param request 待保存或更新的 RuleFlowEntity
     * @return 保存后的实体对象ID
     * @throws IllegalArgumentException 如果 Key 字段在更新时被修改、实体/Key为空，或试图创建已存在的初始版本
     * @throws NoSuchElementException   如果尝试更新但实体ID不存在
     */
    @Transactional(rollbackFor = Exception.class)
    public Long saveOrUpdateNoDeployed(RuleFlowVO request) {
        Long id;
        if (request.getId() == null) {
            // 必须检查版本0是否已存在，以避免联合唯一约束冲突。
            RuleFlowEntity flowEntity = ruleFlowRepository.findByKey(request.getKey()).orElse(null);
            if (flowEntity != null) {
                throw new IllegalArgumentException(String.format("规则流编码[%s]已存在！", request.getKey()));
            }
            RuleFlowEntity entity = new RuleFlowEntity();
            entity.setKey(request.getKey());
            entity.setName(request.getName());
            entity.setDescription(request.getDescription());
            entity.setDraftContent(request.getDraftContent());
            entity.setStatus(RuleFlowStatus.UNDEPLOYED);
            ruleFlowRepository.save(entity);
            id = entity.getId();
        } else {
            // 更新现有规则流的指定版本
            RuleFlowEntity oldEntity = ruleFlowRepository.findById(request.getId())
                    .orElseThrow(() -> new NoSuchElementException(String.format("未找到ID为[%s]的数据！", request.getId())));
            // 检查 Key 是否被修改 (Key是不可变的)
            if (!oldEntity.getKey().equals(request.getKey())) {
                throw new IllegalArgumentException(String.format("规则流编码[%s]是不可变的,不能修改为[%s]！", oldEntity.getKey(), request.getKey()));
            }
            oldEntity.setName(request.getName());
            oldEntity.setDescription(request.getDescription());
            oldEntity.setDraftContent(request.getDraftContent());
            ruleFlowRepository.save(oldEntity);
            id = oldEntity.getId();
        }
        return id;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean deployById(Long id) {
        RuleFlowEntity baseEntity = ruleFlowRepository.findById(id).orElse(null);
        if (Objects.nonNull(baseEntity)) {
            baseEntity.setContent(baseEntity.getDraftContent());
            baseEntity.setStatus(RuleFlowStatus.DEOPLOYED);
            ruleFlowRepository.save(baseEntity);
            // TODO 处理定时启动节点
            TransactionOptDelayerHolder.executeAfterTransactionCommit(() -> applicationEventPublisher.publishEvent(RemoveRuleFlowCacheEvent.create(baseEntity.getKey())));
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean disableById(Long id) {
        RuleFlowEntity baseEntity = ruleFlowRepository.findById(id).orElse(null);
        if (Objects.nonNull(baseEntity)) {
            TransactionOptDelayerHolder.executeAfterTransactionCommit(() -> applicationEventPublisher.publishEvent(RemoveRuleFlowCacheEvent.create(baseEntity.getKey())));
            baseEntity.setStatus(RuleFlowStatus.DISABLED);
            ruleFlowRepository.save(baseEntity);
            return true;
        }
        return false;
    }

}