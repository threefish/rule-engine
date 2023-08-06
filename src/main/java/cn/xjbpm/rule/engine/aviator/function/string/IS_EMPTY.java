package cn.xjbpm.rule.engine.aviator.function.string;

import cn.hutool.core.util.StrUtil;
import cn.xjbpm.rule.engine.aviator.function.AbstractBaseFunction;
import com.googlecode.aviator.runtime.type.AviatorBoolean;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/7/5
 */
@SuppressWarnings("all")
public class IS_EMPTY extends AbstractBaseFunction {

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        String value = arg1.getValue(env).toString();
        if (value == null) {
            return AviatorBoolean.TRUE;
        }
        return AviatorBoolean.valueOf(StrUtil.isEmpty(value));
    }

}
