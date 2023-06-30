package com.myflow.rule.translate;

import com.myflow.rule.enums.OperatorType;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/6/30
 */
public class ListTranslate extends AbstractTranslate {


    private static final Map<OperatorType, OperatorSupplier> OPERATOR_TYPE_CACHE = new HashMap() {
        {

            put(OperatorType.IS_EMPTY, (OperatorSupplier) rule -> String.format("IS_EMPTY(%s,'list')", rule.getName()));
            put(OperatorType.IS_NOT_EMPTY, (OperatorSupplier) rule -> String.format("!IS_EMPTY(%s,'list')", rule.getName()));

            put(OperatorType.CONTAINS, (OperatorSupplier) rule -> String.format("CONTAINS(%s,%s)", rule.getName(), rule.getValue()));
            put(OperatorType.NOT_CONTAINS, (OperatorSupplier) rule -> String.format("NOT_CONTAINS(%s,%s)", rule.getName(), rule.getValue()));

            put(OperatorType.IS_NULL, (OperatorSupplier) rule -> String.format("%s==null", rule.getName()));
            put(OperatorType.IS_NOT_NULL, (OperatorSupplier) rule -> String.format("%s!=null", rule.getName()));
        }
    };


    @Override
    protected OperatorSupplier getOperatorSupplier(OperatorType operator) {
        return OPERATOR_TYPE_CACHE.get(operator);
    }

}
