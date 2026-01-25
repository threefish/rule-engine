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

import cn.xjbpm.rule.common.utils.TimeFormatUtil;
import cn.xjbpm.rule.dto.RuleFlowStatus;
import cn.xjbpm.rule.repository.entity.ExcuteLogEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2025/12/22
 */
public class ExcuteLogVO {

    @Data
    public static class PageQuery {
        private String ruleFlowKey;
        private String requestId;
    }


    @Data
    public static class RetryRuleFlowVO {

        @NotNull
        private Long id;

        /**
         * 采用最新的规则文件
         */
        @NotNull
        private Boolean useLatest;
    }


    @Data
    public static class Detail {

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private Long id;

        /**
         * 规则流编码
         */
        private String ruleFlowKey;

        /**
         * 唯一标识
         */
        private String requestId;

        /**
         * 名称
         */
        private Long timeConsuming;
        private String timeConsumingStr;


        private String errorMessage;

        /**
         * 执行记录(大文本字段）
         */
        private String content;

        /**
         * 执行结果
         */
        private RuleFlowStatus status;

        /**
         * 创建时间，自动填充
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private LocalDateTime createTime;

        /**
         * 更新时间，自动填充
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private LocalDateTime updateTime;


        public static Detail create(ExcuteLogEntity entity) {
            if (entity == null) {
                return null;
            }
            Detail detail = new Detail();
            detail.setId(entity.getId());
            detail.setRuleFlowKey(entity.getRuleFlowKey());
            detail.setRequestId(entity.getRequestId());
            detail.setTimeConsuming(entity.getTimeConsuming());
            detail.setTimeConsumingStr(TimeFormatUtil.formatMs(entity.getTimeConsuming()));
            detail.setErrorMessage(entity.getErrorMessage());
            detail.setContent(entity.getContent());
            detail.setStatus(entity.getStatus());
            detail.setCreateTime(entity.getCreateTime());
            return detail;
        }
    }
}
