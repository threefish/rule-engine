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
import cn.xjbpm.rule.repository.enums.RuleFlowStatus;
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
        name = "rule_flow",
        uniqueConstraints = {
                @UniqueConstraint(name = "uc_flow_key_version", columnNames = {"flow_key", "version"})
        }
)
@Data
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class RuleFlowEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 唯一标识
     */
    @Column(name = "flow_key", nullable = false, unique = true, length = 20)
    private String key;

    @Column(length = 50)
    @Enumerated(EnumType.STRING)
    private TriggerMode triggerMode;

    /**
     * 名称
     */
    @Column(name = "name", nullable = false, length = 20)
    private String name;

    /**
     * 描述（200字）
     */
    @Column(name = "description", length = 200)
    private String description;

    /**
     * 状态
     */
    @Column(name = "status", columnDefinition = "varchar(10)  not null default 'UNDEPLOYED'")
    @Enumerated(EnumType.STRING)
    private RuleFlowStatus status;

    /**
     * 规则规则流内容（大文本字段）
     */
    @Lob
    @Column(name = "content", columnDefinition = "LONGTEXT")
    private String content;

    /**
     * 规则规则流内容（草稿大文本字段）
     */
    @Lob
    @Column(name = "draft_content", columnDefinition = "LONGTEXT")
    private String draftContent;

    /**
     * 创建时间，自动填充
     */
    @Column(name = "create_time", nullable = false)
    @CreatedDate
    private LocalDateTime createTime;

    /**
     * 更新时间，自动填充
     */
    @Column(name = "update_time", nullable = false)
    @LastModifiedDate
    private LocalDateTime updateTime;


}