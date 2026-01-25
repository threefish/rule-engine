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
package cn.xjbpm.rule.engine.aviator.functions;


import cn.xjbpm.rule.common.utils.StringUtils;
import cn.xjbpm.rule.engine.aviator.annotation.FunctionDoc;
import cn.xjbpm.rule.engine.aviator.annotation.FunctionNamespace;
import org.springframework.util.Assert;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * @author 黄川 huchuc@vip.qq.com
 */
@FunctionNamespace(name = "crypto")
@SuppressWarnings("all")
public class CryptoUtils {

    @FunctionDoc(value = "crypto.md5(var1)", description = "MD5加密，返回16位小写十六进制字符串", example = "crypto.md5('http://www.jd.com')")
    public static String md5(String message) {
        Assert.isTrue(StringUtils.isNotBlank(message), "crypto.md5(var1)  var1参数不能为空");
        return cn.hutool.crypto.digest.MD5.create().digestHex(message);
    }

    @FunctionDoc(value = "crypto.sha256(var1)", description = "SHA256加密，返回64位小写十六进制字符串", example = "crypto.sha256('http://www.jd.com')")
    public static String sha256(String message) throws Exception {
        Assert.isTrue(StringUtils.isNotBlank(message), "crypto.sha256(var1)  var1参数不能为空");
        // 创建一个SHA-256消息摘要对象
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        // 将输入字符串转换为字节串并更新消息摘要对象
        messageDigest.update(message.getBytes(StandardCharsets.UTF_8));
        // 获取哈希值的字节数组表示
        byte[] hashedBytes = messageDigest.digest();
        // 将字节数组转换为十六进制字符串
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashedBytes) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }

    @FunctionDoc(value = "crypto.hmacSha256(var1,var2)", description = "HMACSHA256加密，返回Base64编码字符串", example = "crypto.hmacSha256('http://www.jd.com','secret')")
    public static String hmacSha256(String message, String secretKey) throws Exception {
        Assert.isTrue(StringUtils.isNotBlank(message), "crypto.hmacSha256(var1,var2) var1参数不能为空");
        Assert.isTrue(StringUtils.isNotBlank(message), "crypto.hmacSha256(var1,var2) var2参数不能为空");
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hmacSha256Bytes = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hmacSha256Bytes);
    }

    //sha1
    @FunctionDoc(value = "crypto.sha1(var1)", description = "SHA1加密，返回40位小写十六进制字符串", example = "crypto.sha1('http://www.jd.com')")
    public static String sha1(String message) throws Exception {
        Assert.isTrue(StringUtils.isNotBlank(message), "crypto.sha1(var1,) var1参数不能为空");
        // 创建一个SHA-1消息摘要对象
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
        // 将输入字符串转换为字节串并更新消息摘要对象
        messageDigest.update(message.getBytes(StandardCharsets.UTF_8));
        // 获取哈希值的字节数组表示
        byte[] hashedBytes = messageDigest.digest();
        // 将字节数组转换为十六进制字符串
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashedBytes) {
            hexString.append(String.format("%02x", b));
            return hexString.toString();
        }
        throw new RuntimeException("SHA-1加密失败");
    }
}