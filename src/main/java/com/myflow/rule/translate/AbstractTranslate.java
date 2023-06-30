package com.myflow.rule.translate;

import com.myflow.rule.Rule;
import com.myflow.rule.enums.OperatorType;
import org.springframework.util.Assert;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/6/30
 */
public abstract class AbstractTranslate {

    protected abstract OperatorSupplier getOperatorSupplier(OperatorType operator);


    protected String translate(Rule rule) {
        OperatorSupplier operatorSupplier = getOperatorSupplier(rule.getOperator());
        Assert.notNull(operatorSupplier, String.format("不支持的操作符号[%s]", rule.getOperator().getValue()));
        return operatorSupplier.get(rule);
    }

}
