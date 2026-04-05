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

import cn.xjbpm.rule.repository.entity.DynamicUrlMappingEntity;
import cn.xjbpm.rule.repository.enums.MappingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author 黄川 huchuc@vip.qq.com
 * 动态URL映射仓储接口
 */
@Repository
public interface DynamicUrlMappingRepository extends JpaRepository<DynamicUrlMappingEntity, Long>, JpaSpecificationExecutor<DynamicUrlMappingEntity> {

    List<DynamicUrlMappingEntity> findByStatus(MappingStatus status);

    Optional<DynamicUrlMappingEntity> findByUrlPathAndHttpMethod(String urlPath, String httpMethod);

    boolean existsByUrlPathAndHttpMethod(String urlPath, String httpMethod);

}
