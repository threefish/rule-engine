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
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
     * 【分页查询 - 仅最新版本】
     * 查找每个 key 下版本号最大的实体，并支持按 key (精确) 和 name (模糊) 过滤。
     * 该查询用于列表展示，确保每个规则流仅显示其最新版本。
     *
     * @param key      规则流程的唯一标识 (精确匹配，可选)
     * @param name     规则流程的名称 (模糊匹配，可选)
     * @param pageable 分页信息
     * @return 仅包含最新版本规则流程实体的分页结果
     */
    @Query("SELECT l FROM RuleFlowEntity l WHERE l.version = (" +
            "  SELECT MAX(sub.version) FROM RuleFlowEntity sub WHERE sub.key = l.key" +
            ") AND (:key IS NULL OR :key = '' OR l.key = :key) " +
            "  AND (:name IS NULL OR :name = '' OR l.name LIKE CONCAT('%', :name, '%'))")
    Page<RuleFlowEntity> findLatestVersionsByFilters(
            @Param("key") String key,
            @Param("name") String name,
            Pageable pageable
    );

    /**
     * 部署规则流程
     *
     * @param id
     * @return
     */
    @Modifying
    @Query("UPDATE RuleFlowEntity l SET l.status = 'DEOPLOYED' " +
            "WHERE l.id = :id")
    int deployById(@Param("id") Long id);


    /**
     * 部署规则流程
     *
     * @param id
     * @return
     */
    @Modifying
    @Query("UPDATE RuleFlowEntity l SET l.status = 'PAUSED' " +
            "WHERE l.id = :id")
    int pausedById(@Param("id") Long id);


    /**
     * 根据规则流程的唯一标识 key 查找最大版本号。
     * 使用 JPQL 的 MAX() 聚合函数。
     *
     * @param key 规则流程的唯一标识
     * @return 最大版本号
     */
    @Query("SELECT MAX(l.version) FROM RuleFlowEntity l WHERE l.key = :key")
    Integer findMaxVersionByKey(@Param("key") String key);
}