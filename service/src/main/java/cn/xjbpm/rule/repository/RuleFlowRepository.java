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

package cn.xjbpm.rule.repository;

import cn.xjbpm.rule.repository.entity.RuleFlowEntity;
import cn.xjbpm.rule.repository.enums.RuleFlowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author 黄川 huchuc@vip.qq.com
 * 规则流仓储接口
 */
@Repository
public interface RuleFlowRepository extends JpaRepository<RuleFlowEntity, Long>, JpaSpecificationExecutor<RuleFlowEntity> {

    /**
     * 根据规则流的唯一标识 key 获取实体
     *
     * @param key
     * @return
     */
    Optional<RuleFlowEntity> findByKey(String key);

    /**
     * 根据规则流的唯一标识 key 和状态 status 获取实体
     *
     * @param key
     * @param status
     * @return
     */
    Optional<RuleFlowEntity> findByKeyAndStatus(String key, RuleFlowStatus status);

}