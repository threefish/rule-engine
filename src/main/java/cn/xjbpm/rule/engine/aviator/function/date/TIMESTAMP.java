package cn.xjbpm.rule.engine.aviator.function.date;

import cn.hutool.core.date.DateUtil;
import cn.xjbpm.rule.engine.aviator.function.AbstractBaseFunction;
import com.googlecode.aviator.runtime.type.AviatorLong;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/10/25
 */
@SuppressWarnings("all")
public class TIMESTAMP extends AbstractBaseFunction {

    @Override
    public AviatorObject call(Map<String, Object> env) {
        return AviatorLong.valueOf(System.currentTimeMillis());
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        String dateValue = String.valueOf(arg1.getValue(env));
        String dateFormat = String.valueOf(arg1.getValue(env));
        return AviatorLong.valueOf(DateUtil.parse(dateValue, dateFormat));
    }
}
