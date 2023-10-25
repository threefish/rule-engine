package cn.xjbpm.rule.engine.aviator.function.string;

import cn.xjbpm.rule.engine.aviator.function.AbstractBaseFunction;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/7/5
 */
public class LOWER extends AbstractBaseFunction {

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        String var1 = arg1.getValue(env).toString();
        return new AviatorString(var1.toLowerCase());
    }

}
