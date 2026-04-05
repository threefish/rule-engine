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
import cn.xjbpm.rule.filter.MessageFilter;
import cn.xjbpm.rule.filter.MessageFilterFactory;
import cn.xjbpm.rule.node.StartNodeProperties;
import cn.xjbpm.rule.node.enums.FilterType;
import cn.xjbpm.rule.node.enums.TriggerMode;
import cn.xjbpm.rule.node.model.TriggerRule;
import cn.xjbpm.rule.utils.RuleParserUtil;
import cn.xjbpm.rule.utils.SpringContextUtil;
import org.quartz.CronExpression;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 自定义属性校验
 *
 * @author 黄川 huchuc@vip.qq.com
 */
public class StartNodeValidator implements NodeValidator<StartNode> {

    /**
     * 需要验证消息过滤规则的触发类型
     */
    private static final Set<TriggerMode> MESSAGE_TRIGGER_MODES = Set.of(
            TriggerMode.RABBIT_MQ,
            TriggerMode.KAFKA,
            TriggerMode.MQTT,
            TriggerMode.ROCKETMQ,
            TriggerMode.DINGTALK,
            TriggerMode.FEISHU
    );

    @Override
    public void check(StartNode startNode) throws Exception {
        Map<String, Object> properties = startNode.getProperties();
        StartNodeProperties startNodeProperties = JsonUtils.json2Obj(JsonUtils.obj2Json(properties), StartNodeProperties.class);
        TriggerMode triggerMode = startNodeProperties.getTriggerMode();
        if (Objects.nonNull(triggerMode)) {
            if (Objects.equals(triggerMode, TriggerMode.SCHEDULED) && !CollectionUtils.isEmpty(startNodeProperties.getTriggers())) {
                validateScheduledTrigger(startNodeProperties);
            }

            if (MESSAGE_TRIGGER_MODES.contains(triggerMode)) {
                validateMessageFilter(startNodeProperties);
            }
        }
    }

    /**
     * 验证定时触发配置
     */
    private void validateScheduledTrigger(StartNodeProperties startNodeProperties) throws Exception {
        List<TriggerRule> triggers = startNodeProperties.getTriggers();
        for (int i = 0; i < triggers.size(); i++) {
            TriggerRule trigger = triggers.get(i);
            String cronExpression = RuleParserUtil.generateCronExpression(trigger);
            if (!CronExpression.isValidExpression(cronExpression)) {
                throw new IllegalArgumentException(String.format("自动触发规则 存在错误 序号:%s ", i + 1));
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

    /**
     * 验证消息过滤规则配置
     */
    private void validateMessageFilter(StartNodeProperties startNodeProperties) throws Exception {
        FilterType filterType = startNodeProperties.getFilterType();
        String filterRule = startNodeProperties.getFilterRule();

        if (filterType == null) {
            filterType = FilterType.NONE;
        }

        if (filterType != FilterType.NONE) {
            if (!StringUtils.hasText(filterRule)) {
                throw new IllegalArgumentException("消息过滤规则不能为空");
            }
            validateFilterRuleSyntax(filterRule, filterType);
        }
    }

    /**
     * 验证过滤规则表达式语法
     */
    private void validateFilterRuleSyntax(String filterRule, FilterType filterType) throws Exception {
        MessageFilterFactory filterFactory = SpringContextUtil.getBean(MessageFilterFactory.class);
        if (filterFactory == null) {
            return;
        }
        MessageFilter filter = filterFactory.getFilter(filterType);
        if (filter == null) {
            throw new IllegalArgumentException(String.format("不支持的过滤类型: %s", filterType));
        }
        filter.matches("{}", filterRule);
        Assert.hasLength(filterRule, "过滤规则不能为空");

    }
}