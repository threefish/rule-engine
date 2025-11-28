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
 * 规则流程执行日志
 */
@Entity
@Table(
        name = "rule_flow_excute_log",
        indexes = {
                @Index(name = "idx_rule_flow_excute_log_request_id", columnList = "request_id", unique = true),
                @Index(name = "idx_rule_flow_key", columnList = "rule_flow_key")
        }
)
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
public class RuleFlowExcuteLogEntity {

    @Id
    @Column(name = "id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    /**
     * 规则流编码
     */
    @Column(name = "rule_flow_key", nullable = false, length = 20)
    private String ruleFlowKey;

    /**
     * 唯一标识
     */
    @Column(name = "request_id", nullable = false, unique = true, length = 20)
    private String requestId;

    /**
     * 名称
     */
    @Column(name = "time_consuming", length = 10)
    private Long timeConsuming;

    /**
     * 版本, 默认0
     */
    @Column(name = "error_message", length = 100)
    private String errorMessage;

    /**
     * 执行记录(大文本字段）
     */
    @Lob
    @Column(name = "content", columnDefinition = "LONGTEXT")
    private String content;

    /**
     * 执行结果
     */
    @Column(name = "success")
    private Boolean success;

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

    public RuleFlowExcuteLogEntity(Long id, String ruleFlowKey, String requestId, Long timeConsuming, Boolean success, LocalDateTime createTime, LocalDateTime updateTime) {
        this.id = id;
        this.ruleFlowKey = ruleFlowKey;
        this.requestId = requestId;
        this.timeConsuming = timeConsuming;
        this.success = success;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }
}