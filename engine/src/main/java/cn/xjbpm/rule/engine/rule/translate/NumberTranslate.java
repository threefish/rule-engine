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
package cn.xjbpm.rule.engine.rule.translate;

import cn.hutool.core.util.StrUtil;
import cn.xjbpm.rule.engine.rule.enums.OperatorType;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/6/30
 */
public class NumberTranslate extends AbstractTranslate {


    private static final Map<OperatorType, OperatorSupplier> OPERATOR_TYPE_CACHE = new HashMap() {
        {
            put(OperatorType.EQ, (OperatorSupplier) rule -> {
                switch (rule.getAssignmentType()) {
                    case VAR:
                        return String.format("%s==%s", rule.getName(), rule.getFieldValue());
                    case CALC:
                        return String.format("%s==%s", rule.getName(), rule.getExpressionValue());
                }
                return String.format("%s==%s", rule.getName(), rule.getValue());
            });
            put(OperatorType.NOT_EQ, (OperatorSupplier) rule -> {
                switch (rule.getAssignmentType()) {
                    case VAR:
                        return String.format("%s!=%s", rule.getName(), rule.getFieldValue());
                    case CALC:
                        return String.format("%s!=%s", rule.getName(), rule.getExpressionValue());
                }
                return String.format("%s!=%s", rule.getName(), rule.getValue());
            });
            put(OperatorType.GT, (OperatorSupplier) rule -> {
                switch (rule.getAssignmentType()) {
                    case VAR:
                        return String.format("%s>%s", rule.getName(), rule.getFieldValue());
                    case CALC:
                        return String.format("%s>%s", rule.getName(), rule.getExpressionValue());
                }
                return String.format("%s>%s", rule.getName(), rule.getValue());
            });
            put(OperatorType.GTE, (OperatorSupplier) rule -> {
                switch (rule.getAssignmentType()) {
                    case VAR:
                        return String.format("%s>=%s", rule.getName(), rule.getFieldValue());
                    case CALC:
                        return String.format("%s>=%s", rule.getName(), rule.getExpressionValue());
                }
                return String.format("%s>=%s", rule.getName(), rule.getValue());
            });
            put(OperatorType.LT, (OperatorSupplier) rule -> {
                switch (rule.getAssignmentType()) {
                    case VAR:
                        return String.format("%s<%s", rule.getName(), rule.getFieldValue());
                    case CALC:
                        return String.format("%s<%s", rule.getName(), rule.getExpressionValue());
                }
                return String.format("%s<%s", rule.getName(), rule.getValue());
            });
            put(OperatorType.LTE, (OperatorSupplier) rule -> {
                switch (rule.getAssignmentType()) {
                    case VAR:
                        return String.format("%s<=%s", rule.getName(), rule.getFieldValue());
                    case CALC:
                        return String.format("%s<=%s", rule.getName(), rule.getExpressionValue());
                }
                return String.format("%s<=%s", rule.getName(), rule.getValue());
            });
            put(OperatorType.IS_NULL, (OperatorSupplier) rule -> String.format("IS_NULL(%s)", rule.getName()));
            put(OperatorType.IS_NOT_NULL, (OperatorSupplier) rule -> String.format("IS_NOT_NULL(%s)", rule.getName()));
            // TODO 范围运算的，暂未实现变量值和计算值
            put(OperatorType.BETWEEN, (OperatorSupplier) rule -> String.format("NUMBER_BETWEEN(%s,%s,%s)", rule.getName(), rule.getValueStart(), rule.getValueEnd()));
            put(OperatorType.NOT_BETWEEN, (OperatorSupplier) rule -> String.format("!NUMBER_BETWEEN(%s,%s,%s)", rule.getName(), rule.getValueStart(), rule.getValueEnd()));
            put(OperatorType.IN_COLLECTION, (OperatorSupplier) rule -> String.format("IN_COLLECTION(%s,'%s')", rule.getName(), StrUtil.join(",", rule.getValues())));
            put(OperatorType.NOT_IN_COLLECTION, (OperatorSupplier) rule -> String.format("!IN_COLLECTION(%s,'%s')", rule.getName(), StrUtil.join(",", rule.getValues())));
        }
    };


    @Override
    protected OperatorSupplier getOperatorSupplier(OperatorType operator) {
        return OPERATOR_TYPE_CACHE.get(operator);
    }
}