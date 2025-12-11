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
package cn.xjbpm.rule.engine.rule;

import cn.xjbpm.rule.engine.rule.enums.*;
import cn.xjbpm.rule.engine.rule.translate.RuleExpressionTranslate;
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
    private OperatorType operator;
    private String descript;
    private String expressionCacheString;


    public String getExpressionCacheString() {
        if (this.expressionCacheString == null) {
            this.expressionCacheString = new RuleExpressionTranslate(this).getExpression();
        }
        return this.expressionCacheString;
    }

}