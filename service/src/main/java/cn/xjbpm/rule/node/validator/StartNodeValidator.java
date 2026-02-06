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

package cn.xjbpm.rule.node.validator;

import cn.xjbpm.rule.common.utils.JsonUtils;
import cn.xjbpm.rule.engine.aviator.AviatorContext;
import cn.xjbpm.rule.engine.aviator.AviatorExecutor;
import cn.xjbpm.rule.engine.definition.model.nodes.StartNode;
import cn.xjbpm.rule.engine.definition.validator.NodeValidator;
import cn.xjbpm.rule.node.StartNodeProperties;
import cn.xjbpm.rule.node.enums.TriggerMode;
import cn.xjbpm.rule.node.model.TriggerRule;
import cn.xjbpm.rule.utils.RuleParserUtil;
import org.quartz.CronExpression;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 自定义属性校验
 *
 * @author 黄川 huchuc@vip.qq.com
 */
public class StartNodeValidator implements NodeValidator<StartNode> {
    @Override
    public void check(StartNode startNode) throws Exception {
        Map<String, Object> properties = startNode.getProperties();
        StartNodeProperties startNodeProperties = JsonUtils.json2Obj(JsonUtils.obj2Json(properties), StartNodeProperties.class);
        TriggerMode triggerMode = startNodeProperties.getTriggerMode();
        if (Objects.equals(triggerMode, TriggerMode.SCHEDULED) && !CollectionUtils.isEmpty(startNodeProperties.getTriggers())) {
            List<TriggerRule> triggers = startNodeProperties.getTriggers();
            for (int i = 0; i < triggers.size(); i++) {
                TriggerRule trigger = triggers.get(i);
                String cronExpression = RuleParserUtil.generateCronExpression(trigger);
                if (!CronExpression.isValidExpression(cronExpression)) {
                    throw new Exception(String.format("自动触发规则 存在错误 序号:%s ", i + 1));
                }
            }
            if (StringUtils.hasText(startNodeProperties.getRequestParams())) {
                try {
                    AviatorExecutor.evaluateString(AviatorContext.create(startNodeProperties.getRequestParams(), null));
                } catch (Exception e) {
                    throw e;
                }
            }
        }
    }
}