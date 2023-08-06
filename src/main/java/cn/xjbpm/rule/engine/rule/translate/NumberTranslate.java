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
            put(OperatorType.EQ, (OperatorSupplier) rule -> String.format("%s==%s", rule.getName(), rule.getValue()));
            put(OperatorType.NOT_EQ, (OperatorSupplier) rule -> String.format("%s!=%s", rule.getName(), rule.getValue()));
            put(OperatorType.GT, (OperatorSupplier) rule -> String.format("%s>%s", rule.getName(), rule.getValue()));
            put(OperatorType.GTE, (OperatorSupplier) rule -> String.format("%s>=%s", rule.getName(), rule.getValue()));
            put(OperatorType.LT, (OperatorSupplier) rule -> String.format("%s<%s", rule.getName(), rule.getValue()));
            put(OperatorType.LTE, (OperatorSupplier) rule -> String.format("%s<=%s", rule.getName(), rule.getValue()));

            put(OperatorType.BETWEEN, (OperatorSupplier) rule -> String.format("BETWEEN(%s,%s,%s)", rule.getName(), rule.getValueStart(), rule.getValueEnd()));
            put(OperatorType.NOT_BETWEEN, (OperatorSupplier) rule -> String.format("NOT_BETWEEN(%s,%s,%s)", rule.getName(), rule.getValueStart(), rule.getValueEnd()));

            put(OperatorType.IS_NULL, (OperatorSupplier) rule -> String.format("IS_NULL(%s)", rule.getName()));
            put(OperatorType.IS_NOT_NULL, (OperatorSupplier) rule -> String.format("IS_NOT_NULL(%s)", rule.getName()));
            put(OperatorType.IN_COLLECTION, (OperatorSupplier) rule -> String.format("IN_COLLECTION(%s,'%s')", rule.getName(), StrUtil.join(",", rule.getValues())));
            put(OperatorType.NOT_IN_COLLECTION, (OperatorSupplier) rule -> String.format("!IN_COLLECTION(%s,'%s')", rule.getName(), StrUtil.join(",", rule.getValues())));
        }
    };


    @Override
    protected OperatorSupplier getOperatorSupplier(OperatorType operator) {
        return OPERATOR_TYPE_CACHE.get(operator);
    }
}
