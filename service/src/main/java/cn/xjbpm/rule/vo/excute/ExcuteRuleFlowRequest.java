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
package cn.xjbpm.rule.vo.excute;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2025/12/5
 */
@Data
public class ExcuteRuleFlowRequest {
    /**
     * 规则流key
     */
    @NotBlank
    private String key;
    /**
     * 请求ID，唯一，每次不一样
     */
    @NotBlank
    private String requestId;
    /**
     * 规则流变量
     */
    @NotNull
    private Map<String, Object> variables;
    /**
     * 重试原始ID
     */
    private Long retryOriginId;
    /**
     * 异步执行
     */
    private boolean asyncExcute;

}
