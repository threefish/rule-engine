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

import cn.hutool.core.collection.CollUtil;
import cn.xjbpm.rule.repository.CredentialsRepository;
import cn.xjbpm.rule.repository.entity.CredentialsEntity;
import cn.xjbpm.rule.repository.enums.CredentialsType;
import cn.xjbpm.rule.utils.FieldUtil;
import cn.xjbpm.rule.vo.CredentialsVO;
import cn.xjbpm.rule.vo.common.OptionsVO;
import jakarta.persistence.criteria.Predicate;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2025/11/27
 */
@Service
@AllArgsConstructor
public class CredentialsService {

    private final CredentialsRepository credentialsRepository;

    public Page<CredentialsEntity> findPage(CredentialsVO.PageQuery request, Pageable pageable) {
        Specification<CredentialsEntity> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(request.getName())) {
                predicates.add(cb.like(FieldUtil.field(root, CredentialsEntity::getName), "%" + request.getName() + "%"));
            }
            if (Objects.nonNull(request.getType())) {
                predicates.add(cb.equal(FieldUtil.field(root, CredentialsEntity::getType), request.getType()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return credentialsRepository.findAll(specification, pageable);
    }

    public Long save(CredentialsVO.Save request) {
        CredentialsEntity credentialsEntity = new CredentialsEntity();
        credentialsEntity.setId(request.getId());
        credentialsEntity.setName(request.getName());
        credentialsEntity.setType(request.getType());
        credentialsEntity.setAuthData(request.getAuthData());
        credentialsRepository.save(credentialsEntity);
        return credentialsEntity.getId();
    }

    public CredentialsVO.Details details(Long id) {
        CredentialsEntity credentialsEntity = credentialsRepository.findById(id).orElse(null);
        return CredentialsVO.Details.createDetailsView(credentialsEntity);
    }

    public List<OptionsVO> options(CredentialsVO.SelectQuery request) {
        Specification<CredentialsEntity> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (Objects.nonNull(request.getId())) {
                predicates.add(FieldUtil.field(root, CredentialsEntity::getId).in(request.getId()));
            } else {
                if (StringUtils.hasText(request.getKeyword())) {
                    predicates.add(cb.like(FieldUtil.field(root, CredentialsEntity::getName), "%" + request.getKeyword() + "%"));
                }
                if (CollUtil.isNotEmpty(request.getTypes())) {
                    predicates.add(FieldUtil.field(root, CredentialsEntity::getType).in(request.getTypes()));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return credentialsRepository.findAll(specification)
                .stream()
                .map(entity -> OptionsVO.of(entity.getName() + "(" + entity.getType().getName() + ")", entity.getId().toString()))
                .collect(Collectors.toList());
    }

    public CredentialsType type(Long id) {
        return credentialsRepository.findById(id).map(CredentialsEntity::getType).orElse(null);
    }
}