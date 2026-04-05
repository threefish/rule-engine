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

package cn.xjbpm.rule.vo;

import cn.xjbpm.rule.repository.entity.DynamicUrlMappingEntity;
import cn.xjbpm.rule.repository.enums.MappingStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author 黄川 huchuc@vip.qq.com
 * 动态URL映射视图对象
 */
@Data
public class DynamicUrlMappingVO {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    @NotBlank(message = "URL路径不能为空")
    private String urlPath;

    @NotBlank(message = "HTTP方法不能为空")
    private String httpMethod;

    @NotNull(message = "规则流KEY")
    private String ruleFlowKey;

    @NotNull(message = "是否异步响应")
    private Boolean async;


    @NotBlank(message = "处理器配置不能为空")
    private String handlerConfig;

    private MappingStatus status;

    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    public static DynamicUrlMappingVO create(DynamicUrlMappingEntity entity) {
        if (entity == null) {
            return null;
        }
        DynamicUrlMappingVO vo = new DynamicUrlMappingVO();
        vo.setId(entity.getId());
        vo.setUrlPath(entity.getUrlPath());
        vo.setHttpMethod(entity.getHttpMethod());
        vo.setRuleFlowKey(entity.getRuleFlowKey());
        vo.setAsync(entity.getAsync());
        vo.setHandlerConfig(entity.getHandlerConfig());
        vo.setStatus(entity.getStatus());
        vo.setDescription(entity.getDescription());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }

    @Data
    public static class PageQuery {
        private String urlPath;
        private String httpMethod;
        private String ruleFlowKey;
        private MappingStatus status;
    }

}
