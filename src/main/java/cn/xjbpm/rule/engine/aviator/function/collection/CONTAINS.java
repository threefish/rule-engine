package cn.xjbpm.rule.engine.aviator.function.collection;

import cn.xjbpm.rule.engine.aviator.function.AbstractBaseFunction;
import com.googlecode.aviator.runtime.type.AviatorBoolean;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/7/5
 */
@SuppressWarnings("all")
public class CONTAINS extends AbstractBaseFunction {

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        String var1 = arg1.getValue(env).toString();
        String var2 = arg2.getValue(env).toString();
        return AviatorBoolean.valueOf(!(var1.indexOf(var2) > -1));
    }

}
