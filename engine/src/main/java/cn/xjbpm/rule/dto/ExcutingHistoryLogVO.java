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
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 */
@Data
public class ExcutingHistoryLogVO {

    private Long id;

    private String requestId;

    private String ruleFlowKey;

    private Map<String, Object> request;

    private Map<String, Object> response;

    private String processModelOriginalJson;

    private long timeConsuming;

    private Boolean success;

    private String errorMessage;

    private List<TraceLog> traceLogs;

    private Map<String, NodeExcution> nodeExcutions;

    public static ExcutingHistoryLogVO create(ExcuteRuleFlowVO.Response processInstance, Map variables, String originalJson) {
        ExcutingHistoryLogVO excutingHistoryLogVO = new ExcutingHistoryLogVO();
        excutingHistoryLogVO.setProcessModelOriginalJson(originalJson);
        excutingHistoryLogVO.setRequest(variables);
        excutingHistoryLogVO.setResponse(processInstance.getResponse());
        excutingHistoryLogVO.setTimeConsuming(processInstance.getTimeConsuming());
        excutingHistoryLogVO.setSuccess(processInstance.getSuccess());
        excutingHistoryLogVO.setErrorMessage(processInstance.getErrorMessage());
        excutingHistoryLogVO.setTraceLogs(processInstance.getTraceLogs());
        excutingHistoryLogVO.setNodeExcutions(processInstance.getNodeExcutions());
        excutingHistoryLogVO.setId(processInstance.getId());
        excutingHistoryLogVO.setRequestId(processInstance.getRequestId());
        excutingHistoryLogVO.setRuleFlowKey(processInstance.getRuleFlowKey());
        return excutingHistoryLogVO;
    }
}
