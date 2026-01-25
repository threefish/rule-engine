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
package cn.xjbpm.rule.repository.entity;

import cn.xjbpm.rule.repository.enums.CredentialsType;
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
 * 外部服务凭据实体
 */
@Entity
@Table(
        name = "rule_flow_credentials"
)
@Data
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class CredentialsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 凭据名称（如：我的GitHub授权）
     */
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    /**
     * 凭据类型标识（如：githubApi, mysqlApi, redisApi）
     */
    @Column(name = "type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private CredentialsType type;

    /**
     * 核心凭据数据
     * 建议存储：经过 AES 加密后的明文 JSON 字符串
     */
    @Column(name = "auth_data",nullable = false, columnDefinition = "LONGTEXT")
    private String authData;

    /**
     * 创建时间，自动填充
     */
    @Column(name = "create_time", nullable = false, updatable = false)
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