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
package cn.xjbpm.rule.engine.runtime.behavior;

import cn.hutool.core.collection.CollUtil;
import cn.xjbpm.rule.common.utils.ActionUtils;
import cn.xjbpm.rule.common.utils.ConditionUtil;
import cn.xjbpm.rule.engine.aviator.AviatorContext;
import cn.xjbpm.rule.engine.aviator.AviatorExecutor;
import cn.xjbpm.rule.engine.definition.model.activity.RuleSetNode;
import cn.xjbpm.rule.engine.rule.Action;
import cn.xjbpm.rule.engine.rule.RuleAction;
import cn.xjbpm.rule.engine.rule.RuleSet;
import cn.xjbpm.rule.engine.rule.enums.ActionType;
import cn.xjbpm.rule.engine.rule.enums.RuleSetType;
import cn.xjbpm.rule.engine.runtime.behavior.holder.EachRowContext;
import cn.xjbpm.rule.engine.runtime.model.FlowContext;
import cn.xjbpm.rule.exception.ActionExcuteException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/30
 */
@Slf4j
@SuppressWarnings("all")
public class RuleSetNodeBehavior implements NodeBehavior {

    private final static String LOOP_OBJECT_KEY = "循环对象";
    private final static String LOOP_OBJECT_INDEX_KEY = "循环对象索引";
    private final RuleSetNode node;

    public RuleSetNodeBehavior(RuleSetNode node) {
        this.node = node;
    }


    @Override
    public void execution(FlowContext context) {
        log.info("开始执行规则集");
        Map<String, Object> variable = context.getVariable();
        List<RuleSet> ruleSets = node.getRuleSet();
        if (CollUtil.isEmpty(ruleSets)) {
            return;
        }
        ruleSetTag:
        for (RuleSet ruleSet : ruleSets) {
            RuleSetType type = ruleSet.getType();
            Object currentVariable = AviatorExecutor.execute(AviatorContext.create(ruleSet.getLoopVariableName(), variable));
            if (type == RuleSetType.LOOP) {
                if (Objects.nonNull(currentVariable)) {
                    Assert.isTrue(currentVariable instanceof List, "循环变量不是一个有效的集合对象");
                }
                if (currentVariable != null) {
                    List list = ((List) currentVariable);
                    EachRowContext eachRowContext = new EachRowContext();
                    List<RuleAction> ruleActions = ruleSet.getRuleActions();
                    for (RuleAction ruleAction : ruleActions) {
                        listTag:
                        for (int index = 0; index < list.size(); index++) {
                            Object object = list.get(index);
                            try {
                                variable.put(LOOP_OBJECT_KEY, object);
                                variable.put(LOOP_OBJECT_INDEX_KEY, index);

                                if (ConditionUtil.resolve(ruleAction.getWhenRule().getExpressionCacheString(), variable)) {
                                    excuteActions(ruleAction.getThenActions(), variable, eachRowContext, object);
                                } else {
                                    excuteActions(ruleAction.getOtherwiseActions(), variable, eachRowContext, object);
                                }

                            } catch (ActionExcuteException excuteException) {
                                if (excuteException.getActionType() == ActionType.CONTINUE) {
                                    continue listTag;
                                } else if (excuteException.getActionType() == ActionType.BREAK) {
                                    break listTag;
                                } else if (excuteException.getActionType() == ActionType.BREAK_RULE_SET) {
                                    break ruleSetTag;
                                }
                            }
                        }
                    }
                    list.removeAll(eachRowContext.getWaitDeleteRows());
                    variable.remove(LOOP_OBJECT_KEY);
                    variable.remove(LOOP_OBJECT_INDEX_KEY);
                }
            } else {
                try {
                    List<RuleAction> ruleActions = ruleSet.getRuleActions();
                    for (RuleAction ruleAction : ruleActions) {
                        if (ConditionUtil.resolve(ruleAction.getWhenRule().getExpressionCacheString(), variable)) {
                            excuteActions(ruleAction.getThenActions(), variable, null, null);
                        } else {
                            excuteActions(ruleAction.getOtherwiseActions(), variable, null, null);
                        }
                    }
                } catch (ActionExcuteException excuteException) {
                    if (excuteException.getActionType() == ActionType.BREAK_RULE_SET) {
                        break ruleSetTag;
                    }
                }
            }
        }
    }

    /**
     * 执行操作
     *
     * @param actions
     * @param variable
     * @param eachRowContext
     * @param object
     */
    private void excuteActions(List<Action> actions, Map<String, Object> variable, EachRowContext eachRowContext, Object object) {
        for (Action action : actions) {
            ActionType actionType = action.getType();
            switch (actionType) {
                case ASSIGNMENT:
                    ActionUtils.assignment(action, variable);
                    break;
                case DELETE:
                    if (eachRowContext != null && object != null) {
                        eachRowContext.addDeleteObject(object);
                    }
                    break;
                case CONTINUE:
                    throw new ActionExcuteException(ActionType.CONTINUE);
                case BREAK:
                    throw new ActionExcuteException(ActionType.BREAK);
                case BREAK_RULE_SET:
                    throw new ActionExcuteException(ActionType.BREAK_RULE_SET);
                default:
                    break;
            }
        }

    }


}