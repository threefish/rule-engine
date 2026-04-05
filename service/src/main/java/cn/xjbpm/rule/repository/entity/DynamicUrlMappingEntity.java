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

package cn.xjbpm.rule.repository.entity;

import cn.xjbpm.rule.repository.enums.AuthType;
import cn.xjbpm.rule.repository.enums.MappingStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * @author 黄川 huchuc@vip.qq.com
 * 动态URL映射实体
 */
@Entity
@Table(
        name = "dynamic_url_mapping",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_url_method", columnNames = {"url_path", "http_method"})
        }
)
@Data
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class DynamicUrlMappingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @Column(nullable = false, length = 255)
    private String urlPath;

    @Column(nullable = false, length = 10)
    private String httpMethod;

    @Column(nullable = false, length = 50)
    private String ruleFlowKey;

    @Column(nullable = false)
    private Boolean async;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String handlerConfig;

    @Column(columnDefinition = "varchar(20) not null default 'ENABLED'")
    @Enumerated(EnumType.STRING)
    private MappingStatus status = MappingStatus.ENABLED;

    @Column(length = 500)
    private String description;

    @Column(columnDefinition = "varchar(20) not null default 'NONE'")
    @Enumerated(EnumType.STRING)
    private AuthType authType = AuthType.NONE;

    @Column(columnDefinition = "LONGTEXT")
    private String authConfig;

    @Column(nullable = false)
    @CreatedDate
    private LocalDateTime createTime;

    @Column(nullable = false)
    @LastModifiedDate
    private LocalDateTime updateTime;

}
