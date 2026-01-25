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

import cn.xjbpm.rule.common.utils.JsonUtils;
import cn.xjbpm.rule.custom.RuleFlowModelCacheManager;
import cn.xjbpm.rule.engine.definition.model.RuleFlowModel;
import cn.xjbpm.rule.engine.definition.model.nodes.StartNode;
import cn.xjbpm.rule.listener.event.RemoveRuleFlowCacheEvent;
import cn.xjbpm.rule.node.StartNodeProperties;
import cn.xjbpm.rule.node.enums.TriggerMode;
import cn.xjbpm.rule.node.model.TriggerRule;
import cn.xjbpm.rule.repository.RuleFlowRepository;
import cn.xjbpm.rule.repository.RuleFlowScheduledRepository;
import cn.xjbpm.rule.repository.entity.RuleFlowEntity;
import cn.xjbpm.rule.repository.entity.RuleFlowScheduledEntity;
import cn.xjbpm.rule.repository.enums.RuleFlowStatus;
import cn.xjbpm.rule.utils.FieldUtil;
import cn.xjbpm.rule.utils.RuleParserUtil;
import cn.xjbpm.rule.utils.SpringContextUtil;
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
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2025/11/22
 */
@Service
@AllArgsConstructor
public class RuleFlowService {

    private final RuleFlowRepository ruleFlowRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final RuleFlowScheduledRepository ruleFlowScheduledRepository;
    private final SchedulerService schedulerService;


    public RuleFlowVO findByKey(String key) {
        RuleFlowEntity entity = ruleFlowRepository.findByKey(key).orElse(null);
        return RuleFlowVO.create(entity);
    }

    /**
     * 根据Key获取规则规则流实体的引用。
     *
     * @param key
     * @return
     */
    public RuleFlowVO findByKeyAndDeployed(String key) {
        RuleFlowEntity entity = ruleFlowRepository.findByKeyAndStatus(key, RuleFlowStatus.DEPLOYED).orElse(null);
        return RuleFlowVO.create(entity);
    }

    /**
     * 分页查询所有规则规则流实体。
     *
     * @param pageable 分页信息 (页码、大小、排序等)
     * @param key
     * @param name
     * @return 规则规则流实体的分页结果
     */
    public Page<RuleFlowEntity> findPage(String key, String name, RuleFlowStatus status, Pageable pageable) {
        Specification<RuleFlowEntity> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(key)) {
                predicates.add(criteriaBuilder.equal(FieldUtil.field(root, RuleFlowEntity::getKey), key));
            }
            if (StringUtils.hasText(name)) {
                predicates.add(criteriaBuilder.like(FieldUtil.field(root, RuleFlowEntity::getName), "%" + name + "%"));
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(FieldUtil.field(root, RuleFlowEntity::getStatus), status));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        return ruleFlowRepository.findAll(specification, pageable);
    }


    @Transactional(rollbackFor = Exception.class)
    public boolean update(RuleFlowVO request) {
        RuleFlowEntity entity = ruleFlowRepository.findByKey(request.getKey()).orElse(null);
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setDraftContent(request.getDraftContent());
        ruleFlowRepository.save(entity);
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean add(RuleFlowVO request) {
        RuleFlowEntity old = ruleFlowRepository.findByKey(request.getKey()).orElse(null);
        Assert.isNull(old, String.format("规则流编码[%s]已存在！", request.getKey()));
        RuleFlowEntity entity = new RuleFlowEntity();
        entity.setKey(request.getKey());
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setDraftContent(request.getDraftContent());
        entity.setStatus(RuleFlowStatus.UNDEPLOYED);
        ruleFlowRepository.save(entity);
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean deployById(Long id, boolean updateContent) {
        RuleFlowEntity baseEntity = ruleFlowRepository.findById(id).orElse(null);
        if (Objects.nonNull(baseEntity)) {
            if (updateContent) {
                baseEntity.setContent(baseEntity.getDraftContent());
            }
            baseEntity.setStatus(RuleFlowStatus.DEPLOYED);
            RuleFlowModel ruleFlowModel = SpringContextUtil.getBean(RuleFlowModelCacheManager.class).convertToModel(baseEntity.getContent());
            StartNode startNode = ruleFlowModel.getStartNode();
            Map<String, Object> properties = startNode.getProperties();
            if (Objects.nonNull(properties)) {
                StartNodeProperties startNodeProperties = JsonUtils.json2Obj(JsonUtils.obj2Json(properties), StartNodeProperties.class);
                TriggerMode triggerMode = startNodeProperties.getTriggerMode();
                List<RuleFlowScheduledEntity> ruleFlowScheduledEntities = new ArrayList<>();
                if (triggerMode == TriggerMode.SCHEDULED && !CollectionUtils.isEmpty(startNodeProperties.getTriggers())) {
                    List<TriggerRule> triggers = startNodeProperties.getTriggers();
                    for (TriggerRule rule : triggers) {
                        RuleFlowScheduledEntity scheduledEntity = new RuleFlowScheduledEntity();
                        scheduledEntity.setRuleFlowKey(baseEntity.getKey());
                        scheduledEntity.setRequestParams(JsonUtils.obj2Json(JsonUtils.json2Obj(startNodeProperties.getRequestParams(), Map.class)));
                        scheduledEntity.setCronExpression(RuleParserUtil.generateCronExpression(rule));
                        ruleFlowScheduledEntities.add(scheduledEntity);
                    }
                    ruleFlowScheduledRepository.deleteAllByRuleFlowKey(baseEntity.getKey());
                    ruleFlowScheduledRepository.saveAll(ruleFlowScheduledEntities);
                }
                TransactionOptDelayerHolder.executeAfterTransactionCommit(() -> schedulerService.refreshRuleFlowTasks(baseEntity.getKey(), ruleFlowScheduledEntities));
            }
            ruleFlowRepository.save(baseEntity);
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
            ruleFlowScheduledRepository.deleteAllByRuleFlowKey(baseEntity.getKey());
            TransactionOptDelayerHolder.executeAfterTransactionCommit(() -> schedulerService.clearScheduledTasks(baseEntity.getKey()));
            return true;
        }
        return false;
    }


}