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

import cn.xjbpm.rule.common.utils.TimeFormatUtil;
import cn.xjbpm.rule.engine.runtime.model.NodeExcution;
import cn.xjbpm.rule.engine.runtime.model.TraceLog;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Collection;
import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2025/12/5
 */
@Data
public class ExcuteRuleFlowResult {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    private String requestId;

    private String ruleFlowKey;

    private Map<String, Object> response;

    private long timeConsuming;

    private String timeConsumingStr;

    private RuleFlowStatus status;

    private String errorMessage;

    private Collection<TraceLog> traceLogs;

    private Map<String, NodeExcution> nodeExcutions;

    private Map<String, Object> nodesData;

    public String getTimeConsumingStr() {
        return TimeFormatUtil.formatMs(timeConsuming);
    }
}
