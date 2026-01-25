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

import cn.xjbpm.rule.dto.ExcuteRuleFlowResult;
import cn.xjbpm.rule.dto.RuleFlowStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2025/12/5
 */
@Data
@SuppressWarnings("all")
public class ExcuteRuleFlowResponse {

    /**
     * 规则流执行记录ID，可用来重试
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    private String requestId;

    private String ruleFlowKey;

    private Map<String, Object> response;

    private long timeConsuming;


    private RuleFlowStatus status;


    private String errorMessage;

    public static ExcuteRuleFlowResponse create(ExcuteRuleFlowResult excuteRuleFlowResponse) {
        ExcuteRuleFlowResponse response = new ExcuteRuleFlowResponse();
        response.setId(excuteRuleFlowResponse.getId());
        response.setRequestId(excuteRuleFlowResponse.getRequestId());
        response.setRuleFlowKey(excuteRuleFlowResponse.getRuleFlowKey());
        response.setResponse(excuteRuleFlowResponse.getResponse());
        response.setTimeConsuming(excuteRuleFlowResponse.getTimeConsuming());
        response.setStatus(excuteRuleFlowResponse.getStatus());
        response.setErrorMessage(excuteRuleFlowResponse.getErrorMessage());
        return response;
    }
}
