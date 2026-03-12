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

import cn.xjbpm.rule.node.enums.TriggerMode;
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
 */
@Entity
@Table(
        name = "rule_flow_start_trigger",
        indexes = {
                @Index(name = "rule_flow_mq_key", columnList = "ruleFlowKey")
        }
)
@Data
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class StartTriggerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 唯一标识
     */
    @Column(name = "rule_flow_key", nullable = false, length = 20)
    private String ruleFlowKey;

    /**
     * 触发类型
     */
    @Column(name = "type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TriggerMode type;

    /**
     * MQ配置JSON信息
     */
    @Lob
    @Column(name = "content", nullable = false, columnDefinition = "LONGTEXT")
    private String content;

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