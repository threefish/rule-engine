package cn.xjbpm.rule.engine.rule.translate;

import cn.hutool.core.util.StrUtil;
import cn.xjbpm.rule.engine.rule.enums.OperatorType;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/6/30
 * ["=", "!=", "like", "notLike", "beginWith", "endWith","doesNotBeginWith","doesNotEndWith", "isNull", "isNotNull", "inCollection", "notInCollection", "isEmpty", "isNotEmpty"],
 */
public class StringTranslate extends AbstractTranslate {

    private static final Map<OperatorType, OperatorSupplier> OPERATOR_TYPE_CACHE = new HashMap() {
        {
            put(OperatorType.EQ, (OperatorSupplier) rule -> String.format("%s == \"%s\"", rule.getName(), rule.getValue()));
            put(OperatorType.NOT_EQ, (OperatorSupplier) rule -> String.format("%s!=\"%s\"", rule.getName(), rule.getValue()));

            put(OperatorType.LIKE, (OperatorSupplier) rule -> String.format("STR_LIKE(%s,%s)", rule.getName(), rule.getValue()));
            put(OperatorType.NOT_LIKE, (OperatorSupplier) rule -> String.format("!STR_LIKE(%s,%s)", rule.getName(), rule.getValue()));

            put(OperatorType.IS_EMPTY, (OperatorSupplier) rule -> String.format("IS_EMPTY(%s)", rule.getName()));
            put(OperatorType.IS_NOT_EMPTY, (OperatorSupplier) rule -> String.format("!IS_EMPTY(%s)", rule.getName()));

            put(OperatorType.BEGIN_WITH, (OperatorSupplier) rule -> String.format("BEGIN_WITH(%s,%s)", rule.getName(), rule.getValue()));
            put(OperatorType.END_WITH, (OperatorSupplier) rule -> String.format("END_WITH(%s,%s)", rule.getName(), rule.getValue()));

            put(OperatorType.DOES_NOT_BEGIN_WITH, (OperatorSupplier) rule -> String.format("!BEGIN_WITH(%s,%s)", rule.getName(), rule.getValue()));
            put(OperatorType.DOES_NOT_END_WITH, (OperatorSupplier) rule -> String.format("!END_WITH(%s,%s)", rule.getName(), rule.getValue()));

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
