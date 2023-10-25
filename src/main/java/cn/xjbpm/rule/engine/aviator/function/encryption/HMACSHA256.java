package cn.xjbpm.rule.engine.aviator.function.encryption;

import cn.xjbpm.rule.engine.aviator.function.AbstractBaseFunction;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;
import lombok.SneakyThrows;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/10/25
 */
@SuppressWarnings("all")
public class HMACSHA256 extends AbstractBaseFunction {


    @SneakyThrows
    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {

        String message = String.valueOf(arg1.getValue(env));
        String secretKey = String.valueOf(arg2.getValue(env));

        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);

        byte[] hmacSha256Bytes = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
        String hmacSha256Base64 = Base64.getEncoder().encodeToString(hmacSha256Bytes);

        return new AviatorString(hmacSha256Base64);
    }
}
