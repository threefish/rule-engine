package cn.xjbpm.rule.engine.runtime.behavior;

import cn.xjbpm.rule.engine.aviator.AviatorContext;
import cn.xjbpm.rule.engine.aviator.AviatorExecutor;
import cn.xjbpm.rule.engine.common.utils.ActionUtils;
import cn.xjbpm.rule.engine.definition.model.Node;
import cn.xjbpm.rule.engine.definition.model.activity.RuleSetNode;
import cn.xjbpm.rule.engine.rule.Action;
import cn.xjbpm.rule.engine.rule.RuleAction;
import cn.xjbpm.rule.engine.rule.RuleSet;
import cn.xjbpm.rule.engine.rule.enums.ActionType;
import cn.xjbpm.rule.engine.rule.enums.RuleSetType;
import cn.xjbpm.rule.engine.runtime.behavior.holder.EachRowContext;
import cn.xjbpm.rule.engine.runtime.context.ExecutionScope;
import cn.xjbpm.rule.engine.runtime.context.ProcessContextHolder;
import cn.xjbpm.rule.engine.runtime.context.ProcessRuntimeContext;
import cn.xjbpm.rule.engine.runtime.entity.ExecutionEntity;
import cn.xjbpm.rule.engine.runtime.exception.ActionExcuteException;
import cn.xjbpm.rule.engine.runtime.util.ConditionUtil;
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
public class RuleSetNodeBehavior extends BaseNodeBehavior {

    private final static String LOOP_OBJECT_KEY = "循环对象";
    private final static String LOOP_OBJECT_INDEX_KEY = "循环对象索引";
    private final RuleSetNode node;

    public RuleSetNodeBehavior(RuleSetNode node) {
        this.node = node;
    }

    @Override
    public void unableToComplete(ExecutionEntity executionEntity) {
        log.info("节点[{}]不满足完成条件，暂挂在此节点", node.getKey());
    }

    @Override
    public void doExecution(ExecutionEntity executionEntity, ExecutionScope executionScope) {
        log.info("开始执行规则集");
        ProcessRuntimeContext context = ProcessContextHolder.getContext();
        Map<String, Object> variable = context.getVariable();
        List<RuleSet> ruleSets = node.getRuleSet();
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


    @Override
    public Node getCurrentNode() {
        return node;
    }


}
