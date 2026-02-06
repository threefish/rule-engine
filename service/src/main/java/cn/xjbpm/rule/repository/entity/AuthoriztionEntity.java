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

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * @author 黄川 huchuc@vip.qq.com
 * 规则规则流实体
 */
@Entity
@Table(
        name = "rule_flow_authoriztion"
)
@Data
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AuthoriztionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 名称
     */
    @Column(name = "name", nullable = false, length = 20)
    private String name;

    /**
     * apiKey
     */
    @Column(name = "api_key", nullable = false, unique = true, length = 40)
    private String apiKey;

    /**
     * 用途描述
     */
    @Column(name = "description", length = 200)
    private String description;

    /**
     * 已启用
     */
    @Column(name = "enabled")
    private Boolean enabled;

    /**
     * 授权详情
     */
    @Column(name = "authoriztion", length = 300)
    private String authoriztion;

    /**
     * 创建时间，自动填充
     */
    @Column(name = "create_time", nullable = false)
    @CreatedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 更新时间，自动填充
     */
    @Column(name = "update_time", nullable = false)
    @LastModifiedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

}