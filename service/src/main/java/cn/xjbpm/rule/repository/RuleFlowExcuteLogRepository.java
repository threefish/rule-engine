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

import cn.xjbpm.rule.repository.entity.RuleFlowExcuteLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author 黄川 huchuc@vip.qq.com
 */
@Repository
public interface RuleFlowExcuteLogRepository extends JpaRepository<RuleFlowExcuteLogEntity, Long> {

    /**
     * 分页查询
     *
     * @param requestId 规则流程的唯一标识 (精确匹配)
     * @param pageable  分页信息
     * @return 分页结果
     */
    @Query("SELECT " +
            " new cn.xjbpm.rule.repository.entity.RuleFlowExcuteLogEntity(l.id, l.ruleFlowKey, l.requestId, l.timeConsuming, l.success, l.createTime, l.updateTime)" +
            " FROM RuleFlowExcuteLogEntity l WHERE l.requestId = :requestId")
    Page<RuleFlowExcuteLogEntity> findAllByRequestId(@Param("requestId") String requestId, Pageable pageable);

    @Query("SELECT " +
            " new cn.xjbpm.rule.repository.entity.RuleFlowExcuteLogEntity(l.id, l.ruleFlowKey, l.requestId, l.timeConsuming, l.success, l.createTime, l.updateTime)" +
            " FROM RuleFlowExcuteLogEntity l WHERE l.ruleFlowKey = :key")
    Page<RuleFlowExcuteLogEntity> findAllByRuleFlowKey(String key, Pageable pageable);


    Optional<RuleFlowExcuteLogEntity> findByRequestId(@Param("requestId") String requestId);


}