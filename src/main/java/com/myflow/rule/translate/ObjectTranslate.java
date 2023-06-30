package com.myflow.rule.translate;

import com.myflow.rule.enums.OperatorType;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/6/30
 */
public class ObjectTranslate extends AbstractTranslate {

    private static final Map<OperatorType, OperatorSupplier> OPERATOR_TYPE_CACHE = new HashMap() {
        {
            put(OperatorType.IS_NULL, (OperatorSupplier) rule -> String.format("%s==null", rule.getName()));
            put(OperatorType.IS_NOT_NULL, (OperatorSupplier) rule -> String.format("%s!=null", rule.getName()));
        }
    };

    protected OperatorSupplier getOperatorSupplier(OperatorType operator) {
        return OPERATOR_TYPE_CACHE.get(operator);
    }

}
