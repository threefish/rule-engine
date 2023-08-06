package cn.xjbpm.rule.engine.aviator.function.collection;

import cn.hutool.core.collection.CollUtil;
import cn.xjbpm.rule.engine.aviator.function.AbstractBaseFunction;
import com.googlecode.aviator.runtime.type.AviatorBoolean;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.List;
import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/7/5
 */
@SuppressWarnings("all")
public class LIST_IS_EMPTY extends AbstractBaseFunction {

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        Object value = arg1.getValue(env);
        if (value == null) {
            return AviatorBoolean.TRUE;
        }
        if (value instanceof List && CollUtil.isEmpty((List) value)) {
            return AviatorBoolean.TRUE;
        }
        return AviatorBoolean.FALSE;
    }

}
