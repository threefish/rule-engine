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

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author 黄川 huchuc@vip.qq.com
 */
@Data
@SuppressWarnings("all")
public class RuleFlowVO {


    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    @NotBlank
    private String key;

    @NotBlank
    private String name;

    private String description;

    private String content;

    @NotBlank
    private String draftContent;

    private RuleFlowStatus status;
    /**
     * 是否有更新
     */
    private boolean hasUpdate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 更新时间，自动填充
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

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
        vo.setDraftContent(entity.getDraftContent());
        vo.setStatus(entity.getStatus());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        vo.setHasUpdate(Objects.equals(entity.getContent(), entity.getDraftContent()) == false);
        return vo;
    }

    public static RuleFlowVO createPageView(RuleFlowEntity entity) {
        if (entity == null) {
            return null;
        }
        RuleFlowVO vo = new RuleFlowVO();
        vo.setId(entity.getId());
        vo.setKey(entity.getKey());
        vo.setName(entity.getName());
        vo.setDescription(entity.getDescription());
        vo.setStatus(entity.getStatus());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        vo.setHasUpdate(Objects.equals(entity.getContent(), entity.getDraftContent()) == false);
        return vo;
    }


}