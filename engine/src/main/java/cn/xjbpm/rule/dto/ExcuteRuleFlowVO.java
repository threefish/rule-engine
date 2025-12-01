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
package cn.xjbpm.rule.dto;

import cn.xjbpm.rule.engine.runtime.model.NodeExcution;
import cn.xjbpm.rule.engine.runtime.model.TraceLog;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2025/11/20
 */
public class ExcuteRuleFlowVO {


    @Data
    public static class Request {
        /**
         * 流程定义key
         */
        @NotBlank
        private String key;

        /**
         * 请求ID
         */
        private String requestId;
        /**
         * 版本号，为null取最新
         */
        private Integer version;
        /**
         * 流程变量
         */
        @NotNull
        private Map<String, Object> variables;
        /**
         * 流程定义内容
         */
        private String content;
        /**
         * 重试原始ID
         */
        private Long retryOriginId;
        /**
         * 重试模式
         */
        private boolean retryMode;
        /**
         * 异步执行
         */
        private boolean asyncExcute;
        /**
         * 跳过节点
         */
        private Set<String> skipNodeIds;
    }


    @Data
    public static class Response {

        private Long id;

        private String requestId;

        private String ruleFlowKey;

        private Map<String, Object> response;

        private long timeConsuming;

        private Boolean success;

        private String errorMessage;

        private List<TraceLog> traceLogs;

        private Map<String, NodeExcution> nodeExcutions;
    }
}