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

package cn.xjbpm.rule.listener;

import cn.xjbpm.rule.dto.ExcuteRuleFlowResult;
import cn.xjbpm.rule.engine.runtime.model.FlowContext;
import cn.xjbpm.rule.event.RuleFlowDebugEvent;
import cn.xjbpm.rule.manager.DebugRealTimeDataManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * @author 黄川 huchuc@vip.qq.com
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RuleFlowDebugEventListener implements ApplicationListener<RuleFlowDebugEvent> {


    private final DebugRealTimeDataManager debugRealTimeDataManager;

    @Override
    public void onApplicationEvent(RuleFlowDebugEvent event) {
        FlowContext flowContext = event.getFlowContext();
        ExcuteRuleFlowResult processInstance = flowContext.getProcessInstance();
        processInstance.setNodesData(flowContext.getNodesData());
        processInstance.setNodeExcutions(flowContext.getNodeExcutions());
        processInstance.setTraceLogs(flowContext.getTraceLogs());
        debugRealTimeDataManager.put(processInstance.getId(), processInstance);
    }
}