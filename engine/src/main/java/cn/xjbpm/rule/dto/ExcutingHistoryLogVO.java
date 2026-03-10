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


package cn.xjbpm.rule.dto;

import cn.xjbpm.rule.engine.runtime.model.NodeExcution;
import cn.xjbpm.rule.engine.runtime.model.TraceLog;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 */
@Data
@NoArgsConstructor
public class ExcutingHistoryLogVO {

    private Long id;

    private String requestId;

    private String ruleFlowKey;

    private Map<String, Object> request;

    private Map<String, Object> response;

    private String ruleFlowOriginalJson;

    private long timeConsuming;

    private RuleFlowStatus status;

    private String errorMessage;

    private Collection<TraceLog> traceLogs;

    private Map<String, NodeExcution> nodeExcutions;

    private Map<String, Object> nodesData;

    private Long retryOriginId;


    public static ExcutingHistoryLogVO create(ExcuteRuleFlowResult response, Map variables, String originalJson, Long retryOriginId) {
        ExcutingHistoryLogVO excutingHistoryLogVO = new ExcutingHistoryLogVO();
        excutingHistoryLogVO.setRuleFlowOriginalJson(originalJson);
        excutingHistoryLogVO.setRequest(variables);
        excutingHistoryLogVO.setResponse(response.getResponse());
        excutingHistoryLogVO.setTimeConsuming(response.getTimeConsuming());
        excutingHistoryLogVO.setStatus(response.getStatus());
        excutingHistoryLogVO.setErrorMessage(response.getErrorMessage());
        excutingHistoryLogVO.setTraceLogs(response.getTraceLogs());
        excutingHistoryLogVO.setNodeExcutions(response.getNodeExcutions());
        excutingHistoryLogVO.setId(response.getId());
        excutingHistoryLogVO.setRequestId(response.getRequestId());
        excutingHistoryLogVO.setRuleFlowKey(response.getRuleFlowKey());
        excutingHistoryLogVO.setRetryOriginId(retryOriginId);
        excutingHistoryLogVO.setNodesData(response.getNodesData());
        return excutingHistoryLogVO;
    }
}