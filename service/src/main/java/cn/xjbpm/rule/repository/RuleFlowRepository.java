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
package cn.xjbpm.rule.repository;

import cn.xjbpm.rule.repository.entity.RuleFlowEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author 黄川 huchuc@vip.qq.com
 * 规则流程仓储接口
 */
@Repository
public interface RuleFlowRepository extends JpaRepository<RuleFlowEntity, Long> {

    /**
     * 根据规则流程的唯一标识 key 查找实体
     *
     * @param key 规则流程的唯一标识
     * @return 包含 RuleFlowEntity 的 Optional 对象
     */
    Optional<RuleFlowEntity> findByKey(String key);

    /**
     * 根据规则流程的唯一标识 key 查找版本最高的实体。
     * Spring Data JPA 自动实现：findFirst (取第一个结果), ByKey (按 key 过滤), OrderByVersionDesc (按 version 降序排序)。
     *
     * @param key 规则流程的唯一标识
     * @return 包含版本最高的 RuleFlowEntity 的 Optional 对象
     */
    Optional<RuleFlowEntity> findFirstByKeyOrderByVersionDesc(String key);

    /**
     * 根据规则流程的唯一标识 key 和版本号 version 获取实体
     *
     * @param key
     * @param version
     * @return
     */
    Optional<RuleFlowEntity> findByKeyAndVersion(String key, Integer version);


    /**
     * 根据 key 进行精确匹配分页查询
     *
     * @param key      规则流程的唯一标识 (精确匹配)
     * @param pageable 分页信息
     * @return 分页结果
     */
    Page<RuleFlowEntity> findByKey(String key, Pageable pageable);

    /**
     * 仅根据 name 进行模糊匹配分页查询
     * 使用 Containing 实现 LIKE %name% 模糊查询
     *
     * @param name     规则流程的名称 (模糊匹配)
     * @param pageable 分页信息
     * @return 分页结果
     */
    Page<RuleFlowEntity> findByNameContaining(String name, Pageable pageable);

    /**
     * 同时根据 key 精确匹配 AND name 模糊匹配进行分页查询
     *
     * @param key      规则流程的唯一标识 (精确匹配)
     * @param name     规则流程的名称 (模糊匹配)
     * @param pageable 分页信息
     * @return 分页结果
     */
    Page<RuleFlowEntity> findByKeyAndNameContaining(String key, String name, Pageable pageable);
}