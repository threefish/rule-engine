package com.myflow.aviator.function.incometax;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorBigInt;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/7/1
 */
@SuppressWarnings("all")
public class GET_FAST_DEDUCTION extends AbstractFunction {
    private static final String FUNCTION_NAME = GET_FAST_DEDUCTION.class.getSimpleName();

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        Object target = arg1.getValue(env);
        int kcs = 0;
        if (target instanceof Number) {
            Number val = ((Number) target);
            if (val.longValue() >= 36000) {
                kcs = 2520;
            } else if (val.longValue() >= 144000) {
                kcs = 16920;
            } else if (val.longValue() >= 300000) {
                kcs = 31920;
            } else if (val.longValue() >= 420000) {
                kcs = 52920;
            } else if (val.longValue() >= 660000) {
                kcs = 85920;
            } else if (val.longValue() >= 960000) {
                kcs = 181920;
            }
        }
        return AviatorBigInt.valueOf(kcs);
    }

    @Override
    public String getName() {
        return FUNCTION_NAME;
    }
}
