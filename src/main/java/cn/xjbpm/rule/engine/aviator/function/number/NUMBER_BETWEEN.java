package cn.xjbpm.rule.engine.aviator.function.number;

import cn.xjbpm.rule.engine.aviator.function.AbstractBaseFunction;
import com.googlecode.aviator.runtime.type.AviatorBoolean;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/8/6
 */
public class NUMBER_BETWEEN extends AbstractBaseFunction {

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2, AviatorObject arg3) {
        Object value = arg1.getValue(env);
        Object startValue = arg2.getValue(env);
        Object endValue = arg3.getValue(env);
        if (value instanceof Number && startValue instanceof Number && endValue instanceof Number) {
            BigDecimal v = new BigDecimal(value.toString());
            BigDecimal start = new BigDecimal(startValue.toString());
            BigDecimal end = new BigDecimal(endValue.toString());
            if (v.compareTo(start) >= 0 && v.compareTo(end) <= 0) {
                return AviatorBoolean.TRUE;
            }
            return AviatorBoolean.FALSE;

        }
        throw new UnsupportedOperationException("值格式不正确");
    }

}

