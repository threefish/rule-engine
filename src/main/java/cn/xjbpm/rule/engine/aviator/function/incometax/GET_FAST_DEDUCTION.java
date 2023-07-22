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
public class GET_FAST_DEDUCTION extends AbstractBaseFunction {

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


}
