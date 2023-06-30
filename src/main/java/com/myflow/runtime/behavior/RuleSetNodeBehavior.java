package com.myflow.runtime.behavior;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.myflow.aviator.AviatorContext;
import com.myflow.aviator.AviatorExecutor;
import com.myflow.common.utils.VariableUtils;
import com.myflow.definition.model.Node;
import com.myflow.definition.model.activity.RuleSetNode;
import com.myflow.rule.RuleAction;
import com.myflow.rule.RuleSet;
import com.myflow.rule.enums.RuleSetType;
import com.myflow.rule.translate.RuleExpressionTranslate;
import com.myflow.runtime.entity.ExecutionEntity;
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

    private final RuleSetNode node;

    public RuleSetNodeBehavior(RuleSetNode node) {
        this.node = node;
    }

    @Override
    public void unableToComplete(ExecutionEntity executionEntity) {
        log.info("节点[{}]不满足完成条件，暂挂在此节点", node.getKey());
    }

    @Override
    public void doExecution(ExecutionEntity executionEntity) {
        log.info("开始执行规则集");
        List<RuleSet> ruleSets = node.getRuleSet();
        for (RuleSet ruleSet : ruleSets) {
            List<RuleAction> ruleActions = ruleSet.getRuleActions();
            for (RuleAction ruleAction : ruleActions) {
                RuleSetType type = ruleSet.getType();
                Map<String, Object> variable = executionEntity.getVariable();
                Object currentVariable = VariableUtils.getByPathVariable(ruleSet.getLoopVariableName(), variable);
                if (type == RuleSetType.LOOP) {
                    if (Objects.nonNull(currentVariable)) {
                        Assert.isTrue(currentVariable instanceof List, "循环变量只能为List结构");
                    }
                    if (currentVariable instanceof List) {
                        List list = ((List<?>) currentVariable);
                        for (int i = 0; i < list.size(); i++) {
                            Map object = BeanUtil.beanToMap(list.get(i));
                            object.put("index", i);
                            variable.put("循环对象", object);
                            RuleExpressionTranslate ruleExpressionTranslate = new RuleExpressionTranslate(ruleAction.getWhenRule());
                            AviatorContext aviatorContext = AviatorContext.builder().expression(ruleExpressionTranslate.getExpression()).cached(true).env(executionEntity.getVariable()).build();
                            if (StrUtil.isBlank(aviatorContext.getExpression())) {
                                log.info("节点:{} 条件表达式为空", ruleAction.getWhenRule().getKey());
                            }
                            if (StrUtil.isBlank(aviatorContext.getExpression()) || AviatorExecutor.executeBoolean(aviatorContext)) {
                                System.out.println("true");
                            }
                        }
                    }
                } else {

                }

            }
        }
    }

    @Override
    public Node getNode() {
        return node;
    }


}
