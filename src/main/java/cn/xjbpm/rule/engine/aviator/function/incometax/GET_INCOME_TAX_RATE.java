package cn.xjbpm.rule.engine.aviator.function.incometax;

import cn.xjbpm.rule.engine.aviator.function.AbstractBaseFunction;
import com.googlecode.aviator.runtime.type.AviatorBigInt;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/7/1
 */
@SuppressWarnings("all")
public class GET_INCOME_TAX_RATE extends AbstractBaseFunction {

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        Object target = arg1.getValue(env);
        int rate = 3;
        if (target instanceof Number) {
            Number val = ((Number) target);
            if (val.longValue() >= 36000) {
                rate = 10;
            } else if (val.longValue() >= 144000) {
                rate = 20;
            } else if (val.longValue() >= 300000) {
                rate = 25;
            } else if (val.longValue() >= 420000) {
                rate = 30;
            } else if (val.longValue() >= 660000) {
                rate = 35;
            } else if (val.longValue() >= 960000) {
                rate = 45;
            }
        }
        return AviatorBigInt.valueOf(rate);
    }


}
