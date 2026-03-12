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
package cn.xjbpm.rule.consumer;

import cn.hutool.core.util.IdUtil;
import cn.xjbpm.rule.common.utils.JsonUtils;
import cn.xjbpm.rule.dto.ExcuteRuleFlow;
import cn.xjbpm.rule.engine.runtime.RuleFlowExcuteService;
import cn.xjbpm.rule.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2026/3/12
 */
@Slf4j
@Service
public class ProcessMessageHelper {


    /**
     * 处理收到的消息并触发规则流执行
     *
     * @param ruleFlowKey 规则流唯一标识
     * @param message     消息内容
     */
    public void processMessage(String ruleFlowKey, String message) {
        try {
            Map<String, Object> variables = parseMessageToVariables(message);
            ExcuteRuleFlow excuteRuleFlow = new ExcuteRuleFlow();
            excuteRuleFlow.setKey(ruleFlowKey);
            excuteRuleFlow.setRequestId(IdUtil.getSnowflakeNextIdStr());
            excuteRuleFlow.setVariables(variables);
            excuteRuleFlow.setAsyncExcute(true);
            SpringContextUtil.getBean(RuleFlowExcuteService.class).startFlow(excuteRuleFlow);
        } catch (Exception e) {
            log.error("处理消息失败: ruleFlowKey={}, error={}", ruleFlowKey, e.getMessage(), e);
        }
    }


    /**
     * 解析消息内容为变量Map
     */
    private Map<String, Object> parseMessageToVariables(String message) {
        Map<String, Object> variables = new HashMap<>();
        try {
            Map<String, Object> parsed = JsonUtils.json2Obj(message, Map.class);
            if (parsed != null) {
                variables.putAll(parsed);
            }
        } catch (Exception e) {
            variables.put("message", message);
        }
        return variables;
    }


}