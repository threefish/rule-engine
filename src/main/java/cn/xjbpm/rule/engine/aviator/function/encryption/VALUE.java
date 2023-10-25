package cn.xjbpm.rule.engine.aviator.function.encryption;

import cn.xjbpm.rule.engine.aviator.function.AbstractBaseFunction;
import com.googlecode.aviator.runtime.type.AviatorNumber;
import com.googlecode.aviator.runtime.type.AviatorObject;
import lombok.SneakyThrows;

import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/10/25
 */
@SuppressWarnings("all")
public class VALUE extends AbstractBaseFunction {

    @SneakyThrows
    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        String value = String.valueOf(arg1.getValue(env));
        return AviatorNumber.valueOf(value);
    }
}
