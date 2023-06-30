package com.myflow.rule;

import com.myflow.rule.enums.AssignmentType;
import com.myflow.rule.enums.CombinatorType;
import com.myflow.rule.enums.RuleType;
import com.myflow.rule.enums.VariableType;
import lombok.Data;

import java.util.List;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/6/29
 */
@Data
public class Rule {

    private String key;
    private CombinatorType combinator;
    private AssignmentType assignmentType;
    private RuleType ruleType;
    private boolean root;
    private List<Rule> rules;

    private String name;
    private String field;
    private String expressionValue;
    private String fieldValue;
    private Object value;
    private Object valueStart;
    private Object valueEnd;
    private List<Object> values;
    private VariableType varType;
    private String operator;
    private String descript;


}
