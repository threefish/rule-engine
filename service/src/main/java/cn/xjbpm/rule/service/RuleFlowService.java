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
import cn.xjbpm.rule.repository.enums.RuleFlowStatus;
import cn.xjbpm.rule.vo.RuleFlowVO;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

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
     * 根据唯一标识 key 查找版本最高的规则流程实体。
     *
     * @param key 规则流程的唯一标识
     * @return 匹配版本最高的 RuleFlowEntity，如果不存在则返回 null
     */
    public RuleFlowVO findByKeyAndMaxVerison(String key) {
        RuleFlowEntity entity = ruleFlowRepository.findFirstByKeyOrderByVersionDesc(key).orElse(null);
        return RuleFlowVO.create(entity);
    }

    public RuleFlowVO findByKeyAndVerison(String key, Integer version) {
        RuleFlowEntity entity = ruleFlowRepository.findByKeyAndVersion(key, version).orElse(null);
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
    public Page<RuleFlowEntity> findPage(Pageable pageable, String key, String name) {
        return ruleFlowRepository.findLatestVersionsByFilters(key, name, pageable);
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
            RuleFlowEntity existingV0 = ruleFlowRepository.findByKeyAndVersion(request.getKey(), 0).orElse(null);
            if (existingV0 != null) {
                throw new IllegalArgumentException(String.format("规则流编码[%s]已存在！", request.getKey()));
            }
            RuleFlowEntity entity = new RuleFlowEntity();
            entity.setKey(request.getKey());
            entity.setName(request.getName());
            entity.setDescription(request.getDescription());
            entity.setContent(request.getContent());
            entity.setVersion(0); // 版本强制从0开始
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
            // 检查状态是否允许修改
            if (oldEntity.getStatus() == RuleFlowStatus.DEOPLOYED || oldEntity.getStatus() == RuleFlowStatus.PAUSED) {
                throw new IllegalArgumentException("当前规则流已部署或暂停！不允许直接修改此版本！");
            }
            oldEntity.setName(request.getName());
            oldEntity.setDescription(request.getDescription());
            oldEntity.setContent(request.getContent());
            ruleFlowRepository.save(oldEntity);
            id = oldEntity.getId();
        }
        return id;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean deploy(Long id) {
        //TODO 其他已部署的低版本需要禁用或者删除
        return ruleFlowRepository.deployById(id) > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean paused(Long id) {
        return ruleFlowRepository.pausedById(id) > 0;
    }

    /**
     * @param request 包含要复制的版本ID和新版本内容的请求
     * @return 新创建的实体ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long saveNewVersion(RuleFlowVO request) {
        RuleFlowEntity baseEntity = ruleFlowRepository.findById(request.getId()).orElse(null);
        Assert.notNull(baseEntity, "未找到ID为[" + request.getId() + "]的数据！");
        Integer version = ruleFlowRepository.findMaxVersionByKey(baseEntity.getKey());
        RuleFlowEntity maxEntity = ruleFlowRepository.findByKeyAndVersion(baseEntity.getKey(), version).orElse(null);
        if (Objects.nonNull(maxEntity) && maxEntity.getStatus() == RuleFlowStatus.UNDEPLOYED) {
            throw new IllegalArgumentException(String.format("当前不是[%s]的最新版本，请返回首页重新进入页面进行规则流编辑！", baseEntity.getKey()));
        }
        RuleFlowEntity entity = new RuleFlowEntity();
        entity.setKey(baseEntity.getKey());
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setContent(request.getContent());
        entity.setVersion(version + 1);
        entity.setStatus(RuleFlowStatus.UNDEPLOYED);
        ruleFlowRepository.save(entity);
        return entity.getId();
    }
}