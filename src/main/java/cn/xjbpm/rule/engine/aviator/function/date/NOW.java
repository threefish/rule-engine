package cn.xjbpm.rule.engine.aviator.function.date;

import cn.hutool.core.date.DateUtil;
import cn.xjbpm.rule.engine.aviator.function.AbstractBaseFunction;
import com.googlecode.aviator.runtime.type.AviatorBigInt;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

import java.util.Date;
import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/10/25
 */
@SuppressWarnings("all")
public class NOW extends AbstractBaseFunction {

    @Override
    public AviatorObject call(Map<String, Object> env) {
        return call(env, new AviatorString("yyyy-MM-dd HH:mm:ss"));
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        String dateFormat = String.valueOf(arg1.getValue(env));
        return new AviatorString(DateUtil.format(new Date(), dateFormat));
    }
}
