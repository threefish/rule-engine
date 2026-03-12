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

import cn.xjbpm.rule.common.utils.JsonUtils;
import cn.xjbpm.rule.custom.RuleFlowModelCacheManager;
import cn.xjbpm.rule.engine.definition.model.RuleFlowModel;
import cn.xjbpm.rule.engine.definition.model.nodes.StartNode;
import cn.xjbpm.rule.listener.event.RemoveRuleFlowCacheEvent;
import cn.xjbpm.rule.node.StartNodeProperties;
import cn.xjbpm.rule.node.enums.TriggerMode;
import cn.xjbpm.rule.node.model.TriggerRule;
import cn.xjbpm.rule.repository.ExcuteLogRepository;
import cn.xjbpm.rule.repository.RuleFlowRepository;
import cn.xjbpm.rule.repository.ScheduledRepository;
import cn.xjbpm.rule.repository.TriggerStartRepository;
import cn.xjbpm.rule.repository.entity.RuleFlowEntity;
import cn.xjbpm.rule.repository.entity.ScheduledEntity;
import cn.xjbpm.rule.repository.entity.StartTriggerEntity;
import cn.xjbpm.rule.repository.enums.RuleFlowStatus;
import cn.xjbpm.rule.utils.FieldUtil;
import cn.xjbpm.rule.utils.RuleParserUtil;
import cn.xjbpm.rule.utils.SpringContextUtil;
import cn.xjbpm.rule.utils.TransactionOptDelayerHolder;
import cn.xjbpm.rule.vo.RuleFlowVO;
import jakarta.persistence.criteria.Predicate;
import jakarta.validation.constraints.NotNull;
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
@SuppressWarnings("all")
public class RuleFlowService {

    private final RuleFlowRepository ruleFlowRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ScheduledRepository scheduledRepository;
    private final TriggerStartRepository triggerStartRepository;
    private final SchedulerService schedulerService;
    private final TriggerConsumerService triggerConsumerService;
    private final ExcuteLogRepository ruleFlowExcuteLogRepository;


    public RuleFlowVO findByKey(String key) {
        RuleFlowEntity entity = ruleFlowRepository.findByKey(key).orElse(null);
        return RuleFlowVO.create(entity);
    }

    /**
     * 根据Key获取已部署状态的规则流
     *
     * @param key 规则流唯一标识
     * @return 规则流视图对象
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
    public boolean disableById(Long id) {
        RuleFlowEntity baseEntity = ruleFlowRepository.findById(id).orElse(null);
        if (Objects.nonNull(baseEntity)) {
            baseEntity.setStatus(RuleFlowStatus.DISABLED);
            ruleFlowRepository.save(baseEntity);
            scheduledRepository.deleteAllByRuleFlowKey(baseEntity.getKey());
            triggerStartRepository.deleteAllByRuleFlowKey(baseEntity.getKey());
            TransactionOptDelayerHolder.executeAfterTransactionCommit(() -> {
                applicationEventPublisher.publishEvent(RemoveRuleFlowCacheEvent.create(baseEntity.getKey()));
                schedulerService.clearScheduledTasks(baseEntity.getKey());
                triggerConsumerService.clearResource(baseEntity.getKey());
            });
            return true;
        }
        return false;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(@NotNull Long id) {
        RuleFlowEntity baseEntity = ruleFlowRepository.findById(id).orElse(null);
        if (Objects.nonNull(baseEntity)) {
            if (disableById(id)) {
                ruleFlowRepository.deleteById(baseEntity.getId());
                ruleFlowExcuteLogRepository.deleteByRuleFlowKey(baseEntity.getKey());
                return true;
            }
        }
        return false;
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
                if (triggerMode == TriggerMode.SCHEDULED) {
                    deployScheduled(baseEntity.getKey(), startNodeProperties);
                } else if (triggerMode == TriggerMode.RABBIT_MQ) {
                    deployRabbitMQ(baseEntity.getKey(), startNodeProperties);
                } else if (triggerMode == TriggerMode.KAFKA) {
                    deployKafka(baseEntity.getKey(), startNodeProperties);
                } else if (triggerMode == TriggerMode.MQTT) {
                    deployMQTT(baseEntity.getKey(), startNodeProperties);
                } else if (triggerMode == TriggerMode.DINGTALK) {
                    deployDingtalk(baseEntity.getKey(), startNodeProperties);
                }
            }
            ruleFlowRepository.save(baseEntity);
            TransactionOptDelayerHolder.executeAfterTransactionCommit(() -> applicationEventPublisher.publishEvent(RemoveRuleFlowCacheEvent.create(baseEntity.getKey())));
        }
        return true;
    }

    private void deployDingtalk(String ruleFlowKey, StartNodeProperties startNodeProperties) {
        Map<String, String> map = new HashMap<>();
        map.put("credentialId", startNodeProperties.getDingtalkCredentialId());

        StartTriggerEntity ruleFlowMQEntity = new StartTriggerEntity();
        ruleFlowMQEntity.setContent(JsonUtils.obj2Json(map));
        ruleFlowMQEntity.setRuleFlowKey(ruleFlowKey);
        ruleFlowMQEntity.setType(TriggerMode.DINGTALK);
        triggerStartRepository.deleteAllByRuleFlowKey(ruleFlowKey);
        triggerStartRepository.save(ruleFlowMQEntity);
        TransactionOptDelayerHolder.executeAfterTransactionCommit(() -> triggerConsumerService.refreshResource(ruleFlowKey, Arrays.asList(ruleFlowMQEntity)));
    }


    /**
     * 部署MQTT触发器配置
     *
     * @param ruleFlowKey         规则流唯一标识
     * @param startNodeProperties 起始节点属性
     */
    private void deployMQTT(String ruleFlowKey, StartNodeProperties startNodeProperties) {
        Map<String, String> map = new HashMap<>();
        map.put("credentialId", startNodeProperties.getMqttCredentialId());
        map.put("topic", startNodeProperties.getMqttTopic());
        map.put("qos", String.valueOf(startNodeProperties.getMqttQos()));
        map.put("cleanSession", String.valueOf(startNodeProperties.isMqttCleanSession()));
        map.put("clientId", startNodeProperties.getMqttClientId());
        map.put("timeout", String.valueOf(startNodeProperties.isMqttTimeout()));
        map.put("maxMessages", String.valueOf(startNodeProperties.getMqttMaxMessages()));

        StartTriggerEntity ruleFlowMQEntity = new StartTriggerEntity();
        ruleFlowMQEntity.setContent(JsonUtils.obj2Json(map));
        ruleFlowMQEntity.setRuleFlowKey(ruleFlowKey);
        ruleFlowMQEntity.setType(TriggerMode.MQTT);
        triggerStartRepository.deleteAllByRuleFlowKey(ruleFlowKey);
        triggerStartRepository.save(ruleFlowMQEntity);
        TransactionOptDelayerHolder.executeAfterTransactionCommit(() -> triggerConsumerService.refreshResource(ruleFlowKey, Arrays.asList(ruleFlowMQEntity)));
    }

    /**
     * 部署Kafka触发器配置
     *
     * @param ruleFlowKey         规则流唯一标识
     * @param startNodeProperties 起始节点属性
     */
    private void deployKafka(String ruleFlowKey, StartNodeProperties startNodeProperties) {
        Map<String, String> map = new HashMap<>();
        map.put("credentialId", startNodeProperties.getKafkaCredentialId());
        map.put("topic", startNodeProperties.getKafkaTopic());
        map.put("groupId", startNodeProperties.getKafkaGroupId());
        map.put("offsetReset", startNodeProperties.getKafkaOffsetReset());
        map.put("autoCommit", String.valueOf(startNodeProperties.isKafkaAutoCommit()));
        map.put("timeout", String.valueOf(startNodeProperties.getKafkaTimeout()));
        map.put("maxPollRecords", String.valueOf(startNodeProperties.getKafkaMaxPollRecords()));

        StartTriggerEntity ruleFlowMQEntity = new StartTriggerEntity();
        ruleFlowMQEntity.setContent(JsonUtils.obj2Json(map));
        ruleFlowMQEntity.setRuleFlowKey(ruleFlowKey);
        ruleFlowMQEntity.setType(TriggerMode.KAFKA);
        triggerStartRepository.deleteAllByRuleFlowKey(ruleFlowKey);
        triggerStartRepository.save(ruleFlowMQEntity);
        TransactionOptDelayerHolder.executeAfterTransactionCommit(() -> triggerConsumerService.refreshResource(ruleFlowKey, Arrays.asList(ruleFlowMQEntity)));
    }


    /**
     * 部署RabbitMQ触发器配置
     *
     * @param ruleFlowKey         规则流唯一标识
     * @param startNodeProperties 起始节点属性
     */
    private void deployRabbitMQ(String ruleFlowKey, StartNodeProperties startNodeProperties) {
        Map<String, String> map = new HashMap<>();
        map.put("credentialId", startNodeProperties.getRabbitMQCredentialId());
        map.put("queueName", startNodeProperties.getRabbitMQQueueName());
        map.put("autoAck", String.valueOf(startNodeProperties.isRabbitMQAutoAck()));
        map.put("prefetchCount", String.valueOf(startNodeProperties.getRabbitMQPrefetchCount()));

        StartTriggerEntity ruleFlowMQEntity = new StartTriggerEntity();
        ruleFlowMQEntity.setContent(JsonUtils.obj2Json(map));
        ruleFlowMQEntity.setRuleFlowKey(ruleFlowKey);
        ruleFlowMQEntity.setType(TriggerMode.RABBIT_MQ);
        triggerStartRepository.deleteAllByRuleFlowKey(ruleFlowKey);
        triggerStartRepository.save(ruleFlowMQEntity);
        TransactionOptDelayerHolder.executeAfterTransactionCommit(() -> triggerConsumerService.refreshResource(ruleFlowKey, Arrays.asList(ruleFlowMQEntity)));
    }

    private void deployScheduled(String ruleFlowKey, StartNodeProperties startNodeProperties) {
        List<ScheduledEntity> ruleFlowScheduledEntities = new ArrayList<>();
        if (!CollectionUtils.isEmpty(startNodeProperties.getTriggers())) {
            List<TriggerRule> triggers = startNodeProperties.getTriggers();
            for (TriggerRule rule : triggers) {
                ScheduledEntity scheduledEntity = new ScheduledEntity();
                scheduledEntity.setRuleFlowKey(ruleFlowKey);
                scheduledEntity.setRequestParams(JsonUtils.obj2Json(JsonUtils.json2Obj(startNodeProperties.getRequestParams(), Map.class)));
                scheduledEntity.setCronExpression(RuleParserUtil.generateCronExpression(rule));
                ruleFlowScheduledEntities.add(scheduledEntity);
            }
            scheduledRepository.deleteAllByRuleFlowKey(ruleFlowKey);
            scheduledRepository.saveAll(ruleFlowScheduledEntities);
        }
        TransactionOptDelayerHolder.executeAfterTransactionCommit(() -> schedulerService.refreshRuleFlowTasks(ruleFlowKey, ruleFlowScheduledEntities));
    }


}