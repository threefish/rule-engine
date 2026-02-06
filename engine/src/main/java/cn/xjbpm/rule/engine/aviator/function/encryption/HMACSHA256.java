/**
 * Copyright 2025 threefish.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.xjbpm.rule.engine.aviator.function.encryption;

import cn.xjbpm.rule.engine.aviator.function.AbstractBaseFunction;
import cn.xjbpm.rule.engine.aviator.function.AviatorExtendFunction;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;
import org.springframework.util.Assert;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * HMAC-SHA256 加密函数
 * 强化了防御性编程，支持 Base64 输出结果，严格执行参数非空校验。
 *
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/10/25
 */
@SuppressWarnings("all")
public class HMACSHA256 extends AbstractBaseFunction {

    private static final String ALGORITHM = "HmacSHA256";

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        Object messageObj = arg1.getValue(env);
        Object secretKeyObj = arg2.getValue(env);


        Assert.notNull(messageObj, String.format("函数 %s 的待加密消息(var1)不能为空", getName()));
        Assert.notNull(secretKeyObj, String.format("函数 %s 的密钥(var2)不能为空", getName()));

        String message = messageObj.toString();
        String secretKey = secretKeyObj.toString();

        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            mac.init(secretKeySpec);

            byte[] hmacSha256Bytes = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            // 返回 Base64 编码的结果
            String hmacSha256Base64 = Base64.getEncoder().encodeToString(hmacSha256Bytes);

            return new AviatorString(hmacSha256Base64);
        } catch (Exception e) {
            // 防呆：处理算法不存在或初始化失败的情况
            throw new RuntimeException(String.format("执行 %s 加密失败。错误信息：%s", getName(), e.getMessage()), e);
        }
    }

    @Override
    public List<AviatorExtendFunction> docs() {
        return Collections.singletonList(
                new AviatorExtendFunction(
                        getName(),
                        String.format("%s(message, secretKey)", getName()),
                        "string",
                        "获取 HMAC-SHA256 加密后的 Base64 字符串。任意参数为 null 将抛出异常。",
                        String.format("%s('hello', 'secret')", getName()))
        );
    }
}