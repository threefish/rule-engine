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
package cn.xjbpm.rule.vo;

import cn.xjbpm.rule.repository.entity.RuleFlowEntity;
import cn.xjbpm.rule.repository.enums.RuleFlowStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author 黄川 huchuc@vip.qq.com
 */
@Data
public class RuleFlowVO {


    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    @NotBlank
    private String key;

    @NotBlank
    private String name;

    private String description;

    @NotBlank
    private String content;

    private Integer version;

    private RuleFlowStatus status;

    private Boolean newVersion;


    public static RuleFlowVO create(RuleFlowEntity entity) {
        if (entity == null) {
            return null;
        }
        RuleFlowVO vo = new RuleFlowVO();
        vo.setId(entity.getId());
        vo.setKey(entity.getKey());
        vo.setName(entity.getName());
        vo.setDescription(entity.getDescription());
        vo.setContent(entity.getContent());
        vo.setVersion(entity.getVersion());
        vo.setStatus(entity.getStatus());
        return vo;
    }


}