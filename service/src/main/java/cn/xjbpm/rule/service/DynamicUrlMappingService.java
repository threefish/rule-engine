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

import cn.xjbpm.rule.manager.DynamicUrlMappingManager;
import cn.xjbpm.rule.repository.DynamicUrlMappingRepository;
import cn.xjbpm.rule.repository.entity.DynamicUrlMappingEntity;
import cn.xjbpm.rule.repository.enums.MappingStatus;
import cn.xjbpm.rule.utils.FieldUtil;
import cn.xjbpm.rule.vo.DynamicUrlMappingVO;
import jakarta.persistence.criteria.Predicate;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 黄川 huchuc@vip.qq.com
 * 动态URL映射服务
 */
@Service
@AllArgsConstructor
@Slf4j
public class DynamicUrlMappingService {

    private final DynamicUrlMappingRepository dynamicUrlMappingRepository;
    private final DynamicUrlMappingManager dynamicUrlMappingManager;

    public DynamicUrlMappingVO findById(Long id) {
        DynamicUrlMappingEntity entity = dynamicUrlMappingRepository.findById(id).orElse(null);
        return DynamicUrlMappingVO.create(entity);
    }

    public Page<DynamicUrlMappingEntity> findPage(String urlPath, String httpMethod,
                                                  String ruleFlowKey,
                                                  MappingStatus status, Pageable pageable) {
        Specification<DynamicUrlMappingEntity> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(urlPath)) {
                predicates.add(criteriaBuilder.like(FieldUtil.field(root, DynamicUrlMappingEntity::getUrlPath), "%" + urlPath + "%"));
            }
            if (StringUtils.hasText(httpMethod)) {
                predicates.add(criteriaBuilder.equal(FieldUtil.field(root, DynamicUrlMappingEntity::getHttpMethod), httpMethod.toUpperCase()));
            }
            if (StringUtils.hasText(ruleFlowKey)) {
                predicates.add(criteriaBuilder.equal(FieldUtil.field(root, DynamicUrlMappingEntity::getRuleFlowKey), ruleFlowKey));
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(FieldUtil.field(root, DynamicUrlMappingEntity::getStatus), status));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        return dynamicUrlMappingRepository.findAll(specification, pageable);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean add(DynamicUrlMappingVO vo) {
        String urlPath = vo.getUrlPath();
        String httpMethod = vo.getHttpMethod().toUpperCase();

        Assert.isTrue(urlPath.startsWith("/"), "URL路径必须以/开头");

        DynamicUrlMappingEntity existing = dynamicUrlMappingRepository
                .findByUrlPathAndHttpMethod(urlPath, httpMethod)
                .orElse(null);
        Assert.isNull(existing, String.format("URL映射已存在: %s %s", httpMethod, urlPath));

        DynamicUrlMappingEntity entity = new DynamicUrlMappingEntity();
        entity.setUrlPath(urlPath);
        entity.setHttpMethod(httpMethod);
        entity.setRuleFlowKey(vo.getRuleFlowKey());
        entity.setAsync(vo.getAsync());

        entity.setHandlerConfig(vo.getHandlerConfig());
        entity.setDescription(vo.getDescription());
        entity.setStatus(MappingStatus.DISABLED);

        dynamicUrlMappingRepository.save(entity);
        log.info("成功添加动态URL映射: {} {}", httpMethod, urlPath);
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean update(DynamicUrlMappingVO vo) {
        DynamicUrlMappingEntity entity = dynamicUrlMappingRepository.findById(vo.getId()).orElse(null);
        Assert.notNull(entity, "URL映射不存在");

        String newUrlPath = vo.getUrlPath();
        String newHttpMethod = vo.getHttpMethod().toUpperCase();

        if (!entity.getUrlPath().equals(newUrlPath) || !entity.getHttpMethod().equalsIgnoreCase(vo.getHttpMethod())) {
            if (entity.getStatus() == MappingStatus.ENABLED) {
                dynamicUrlMappingManager.unregisterMapping(entity.getUrlPath(), entity.getHttpMethod());
            }

            DynamicUrlMappingEntity existing = dynamicUrlMappingRepository
                    .findByUrlPathAndHttpMethod(newUrlPath, newHttpMethod)
                    .orElse(null);
            Assert.isNull(existing, String.format("URL映射已存在: %s %s", newHttpMethod, newUrlPath));

            entity.setUrlPath(newUrlPath);
            entity.setHttpMethod(newHttpMethod);
        }

        entity.setRuleFlowKey(vo.getRuleFlowKey());
        entity.setAsync(vo.getAsync());
        entity.setHandlerConfig(vo.getHandlerConfig());
        entity.setDescription(vo.getDescription());

        dynamicUrlMappingRepository.save(entity);

        if (entity.getStatus() == MappingStatus.ENABLED) {
            dynamicUrlMappingManager.registerMapping(entity);
        }

        log.info("成功更新动态URL映射: {} {}", newHttpMethod, newUrlPath);
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean enable(Long id) {
        DynamicUrlMappingEntity entity = dynamicUrlMappingRepository.findById(id).orElse(null);
        Assert.notNull(entity, "URL映射不存在");

        if (entity.getStatus() == MappingStatus.ENABLED) {
            return true;
        }

        entity.setStatus(MappingStatus.ENABLED);
        dynamicUrlMappingRepository.save(entity);

        dynamicUrlMappingManager.registerMapping(entity);

        log.info("成功启用动态URL映射: {} {}", entity.getHttpMethod(), entity.getUrlPath());
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean disable(Long id) {
        DynamicUrlMappingEntity entity = dynamicUrlMappingRepository.findById(id).orElse(null);
        Assert.notNull(entity, "URL映射不存在");

        if (entity.getStatus() == MappingStatus.DISABLED) {
            return true;
        }

        entity.setStatus(MappingStatus.DISABLED);
        dynamicUrlMappingRepository.save(entity);

        dynamicUrlMappingManager.unregisterMapping(entity.getUrlPath(), entity.getHttpMethod());

        log.info("成功禁用动态URL映射: {} {}", entity.getHttpMethod(), entity.getUrlPath());
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long id) {
        DynamicUrlMappingEntity entity = dynamicUrlMappingRepository.findById(id).orElse(null);
        Assert.notNull(entity, "URL映射不存在");

        if (entity.getStatus() == MappingStatus.ENABLED) {
            dynamicUrlMappingManager.unregisterMapping(entity.getUrlPath(), entity.getHttpMethod());
        }

        dynamicUrlMappingRepository.deleteById(id);

        log.info("成功删除动态URL映射: {} {}", entity.getHttpMethod(), entity.getUrlPath());
        return true;
    }

    public List<DynamicUrlMappingEntity> findAllEnabled() {
        return dynamicUrlMappingRepository.findByStatus(MappingStatus.ENABLED);
    }

    public void registerMappingOnStartup(DynamicUrlMappingEntity entity) {
        try {
            dynamicUrlMappingManager.registerMapping(entity);
            log.info("启动时注册动态URL映射: {} {}", entity.getHttpMethod(), entity.getUrlPath());
        } catch (Exception e) {
            log.error("启动时注册动态URL映射失败: {} {}", entity.getHttpMethod(), entity.getUrlPath(), e);
        }
    }

}
