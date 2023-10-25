package cn.xjbpm.rule.engine.aviator.function.encryption;

import cn.xjbpm.rule.engine.aviator.function.AbstractBaseFunction;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;
import lombok.SneakyThrows;

import java.net.URLEncoder;
import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/10/25
 */
@SuppressWarnings("all")
public class URLENCODE extends AbstractBaseFunction {

    @SneakyThrows
    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        String message = String.valueOf(arg1.getValue(env));
        return new AviatorString(URLEncoder.encode(message, "utf-8"));
    }
}
