/*
 * Copyright 2025 threefish.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.xjbpm.rule.engine.aviator.function.encryption;

import cn.xjbpm.rule.engine.aviator.function.AbstractBaseFunction;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/10/25
 */
@SuppressWarnings("all")
public class SHA256 extends AbstractBaseFunction {


    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        String message = String.valueOf(arg1.getValue(env));
        return new AviatorString(sha256Encode(message));
    }

    private String sha256Encode(String input) {
        try {
            // 创建一个SHA-256消息摘要对象
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            // 将输入字符串转换为字节串并更新消息摘要对象
            messageDigest.update(input.getBytes(StandardCharsets.UTF_8));
            // 获取哈希值的字节数组表示
            byte[] hashedBytes = messageDigest.digest();
            // 将字节数组转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashedBytes) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("无法找到SHA-256算法", e);
        }
    }
}