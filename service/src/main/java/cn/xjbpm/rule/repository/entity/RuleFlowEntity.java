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

import jakarta.persistence.*;
import lombok.Data;

/**
 * @author 黄川 huchuc@vip.qq.com
 * 规则流程实体
 */
@Entity
@Table(
        name = "rule_flow",
        indexes = {
                @Index(name = "idx_rule_flow_key", columnList = "flow_key", unique = true)
        }
)
@Data
public class RuleFlowEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 唯一标识
     */
    @Column(name = "flow_key", nullable = false, unique = true, length = 100)
    private String key;

    /**
     * 名称
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * 版本, 默认0
     */
    @Column(name = "version", nullable = false)
    private Integer version = 0;

    /**
     * 描述（200字）
     */
    @Column(name = "description", length = 200)
    private String description;

    /**
     * 规则流程内容（大文本字段）
     */
    @Lob
    @Column(name = "content", columnDefinition = "LONGTEXT")
    private String content;


}