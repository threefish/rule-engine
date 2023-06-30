package com.myflow.rule.translate;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.myflow.rule.Rule;
import com.myflow.rule.enums.CombinatorType;
import com.myflow.rule.enums.RuleType;
import com.myflow.rule.enums.VariableType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/6/30
 */
@SuppressWarnings("all")
public class RuleExpressionTranslate {


    public final Rule rule;


    private List<String> expressions = new ArrayList<>();
    private String expression;

    public RuleExpressionTranslate(Rule rule) {
        this.rule = rule;
    }

    public String getExpression() {
        if (StrUtil.isBlank(expression)) {
            expressions.add(rule.getCombinator().name());
            if (rule.isRoot() || rule.getRuleType() == RuleType.group) {
                if (CollUtil.isNotEmpty(rule.getRules())) {
                    expressions.add("(");
                    calcRules(rule.getRules());
                    expressions.add(")");
                }
            } else {
                calcRule();
            }
            if (expressions.size() > 0) {
                String start = expressions.get(0);
                if (CombinatorType.AND.toString().equals(start) || CombinatorType.OR.toString().equals(start)) {
                    expressions.remove(0);
                }
            }
            this.expression = StrUtil.join(" ", expressions);
        }
        return this.expression;
    }

    private void calcRule() {
        VariableType varType = rule.getVarType();
        AbstractTranslate translate = varType.getTranslate();
        expressions.add(translate.translate(rule));
    }

    private void calcRules(List<Rule> rules) {
        for (int i = 0; i < rules.size(); i++) {
            Rule childRule = rules.get(i);
            if (i != 0) {
                expressions.add(rule.getCombinator().name());
            }
            expressions.add(new RuleExpressionTranslate(childRule).getExpression());
        }
    }


}
