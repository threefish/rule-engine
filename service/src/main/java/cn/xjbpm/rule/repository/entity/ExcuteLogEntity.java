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

import cn.xjbpm.rule.dto.RuleFlowStatus;
import cn.xjbpm.rule.service.storage.StorageProvider;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * @author 黄川 huchuc@vip.qq.com
 * 规则规则流执行日志
 */
@Entity
@Table(
        name = "rule_flow_excute_log",
        indexes = {
                @Index(columnList = "requestId", unique = true),
                @Index(columnList = "ruleFlowKey")
        }
)
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
public class ExcuteLogEntity {

    @Id
    @Column
    private Long id;

    /**
     * 规则流编码
     */
    @Column(nullable = false, length = 20)
    private String ruleFlowKey;

    /**
     * 唯一标识
     */
    @Column(nullable = false, unique = true, length = 32)
    private String requestId;

    /**
     * 耗时
     */
    @Column(length = 10)
    private Long timeConsuming;

    @Column(length = 100)
    private String errorMessage;

    @Column(length = 100)
    private String snapshotPath;

    /**
     * 执行记录(采用文件方式存储与获取）
     *
     * @see StorageProvider
     */
    @Transient
    private String content;

    @Column
    private RuleFlowStatus status;

    /**
     * 创建时间，自动填充
     */
    @Column(nullable = false)
    @CreatedDate
    private LocalDateTime createTime;

    /**
     * 更新时间，自动填充
     */
    @Column(nullable = false)
    @LastModifiedDate
    private LocalDateTime updateTime;

}