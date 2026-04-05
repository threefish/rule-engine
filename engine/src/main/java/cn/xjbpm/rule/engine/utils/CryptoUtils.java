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

package cn.xjbpm.rule.engine.utils;

import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.crypto.asymmetric.SM2;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.crypto.symmetric.AES;
import cn.hutool.crypto.symmetric.SM4;
import cn.xjbpm.rule.engine.definition.model.nodes.CryptoNode.Encoding;

import java.nio.charset.Charset;
import java.util.Base64;

/**
 * 加解密工具类
 * 支持AES、RSA、MD5、SHA256以及国密算法（SM2/SM3/SM4）
 *
 * @author 黄川 huchuc@vip.qq.com
 */
public class CryptoUtils {

    private CryptoUtils() {
    }

    /**
     * AES加密
     */
    public static String aesEncrypt(String plainText, String key, Encoding encoding, Charset charset) {
        AES aes = SecureUtil.aes(key.getBytes(charset));
        byte[] encrypted = aes.encrypt(plainText.getBytes(charset));
        return encode(encrypted, encoding);
    }

    /**
     * AES解密
     */
    public static String aesDecrypt(String cipherText, String key, Encoding encoding, Charset charset) {
        AES aes = SecureUtil.aes(key.getBytes(charset));
        byte[] decrypted = aes.decrypt(decode(cipherText, encoding));
        return new String(decrypted, charset);
    }

    /**
     * SM4加密
     */
    public static String sm4Encrypt(String plainText, String key, Encoding encoding, Charset charset) {
        SM4 sm4 = SmUtil.sm4(key.getBytes(charset));
        byte[] encrypted = sm4.encrypt(plainText.getBytes(charset));
        return encode(encrypted, encoding);
    }

    /**
     * SM4解密
     */
    public static String sm4Decrypt(String cipherText, String key, Encoding encoding, Charset charset) {
        SM4 sm4 = SmUtil.sm4(key.getBytes(charset));
        byte[] decrypted = sm4.decrypt(decode(cipherText, encoding));
        return new String(decrypted, charset);
    }

    /**
     * RSA加密
     */
    public static String rsaEncrypt(String plainText, String publicKey, Encoding encoding, Charset charset) {
        RSA rsa = SecureUtil.rsa(null, publicKey);
        byte[] encrypted = rsa.encrypt(plainText.getBytes(charset), KeyType.PublicKey);
        return encode(encrypted, encoding);
    }

    /**
     * RSA解密
     */
    public static String rsaDecrypt(String cipherText, String privateKey, Encoding encoding, Charset charset) {
        RSA rsa = SecureUtil.rsa(privateKey, null);
        byte[] decrypted = rsa.decrypt(decode(cipherText, encoding), KeyType.PrivateKey);
        return new String(decrypted, charset);
    }

    /**
     * RSA签名
     */
    public static String rsaSign(String data, String privateKey, Encoding encoding, Charset charset) {
        RSA rsa = SecureUtil.rsa(privateKey, null);
        byte[] sign = rsa.encrypt(data.getBytes(charset), KeyType.PrivateKey);
        return encode(sign, encoding);
    }

    /**
     * RSA验签
     */
    public static boolean rsaVerify(String data, String sign, String publicKey, Encoding encoding, Charset charset) {
        RSA rsa = SecureUtil.rsa(null, publicKey);
        try {
            byte[] decrypted = rsa.decrypt(decode(sign, encoding), KeyType.PublicKey);
            return new String(decrypted, charset).equals(data);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * SM2加密
     */
    public static String sm2Encrypt(String plainText, String publicKey, Encoding encoding, Charset charset) {
        SM2 sm2 = SmUtil.sm2(null, publicKey);
        byte[] encrypted = sm2.encrypt(plainText.getBytes(charset), KeyType.PublicKey);
        return encode(encrypted, encoding);
    }

    /**
     * SM2解密
     */
    public static String sm2Decrypt(String cipherText, String privateKey, Encoding encoding, Charset charset) {
        SM2 sm2 = SmUtil.sm2(privateKey, null);
        byte[] decrypted = sm2.decrypt(decode(cipherText, encoding), KeyType.PrivateKey);
        return new String(decrypted, charset);
    }

    /**
     * SM2签名
     */
    public static String sm2Sign(String data, String privateKey, Encoding encoding, Charset charset) {
        SM2 sm2 = SmUtil.sm2(privateKey, null);
        byte[] sign = sm2.encrypt(data.getBytes(charset), KeyType.PrivateKey);
        return encode(sign, encoding);
    }

    /**
     * SM2验签
     */
    public static boolean sm2Verify(String data, String sign, String publicKey, Encoding encoding, Charset charset) {
        SM2 sm2 = SmUtil.sm2(null, publicKey);
        try {
            byte[] decrypted = sm2.decrypt(decode(sign, encoding), KeyType.PublicKey);
            return new String(decrypted, charset).equals(data);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * MD5哈希
     */
    public static String md5Hash(String data, Charset charset) {
        return DigestUtil.md5Hex(data, charset);
    }

    /**
     * SHA256哈希
     */
    public static String sha256Hash(String data, Charset charset) {
        return DigestUtil.sha256Hex(data, charset.name());
    }

    /**
     * SM3哈希
     */
    public static String sm3Hash(String data, Charset charset) {
        return SmUtil.sm3().digestHex(data, charset);
    }

    /**
     * 编码
     */
    private static String encode(byte[] data, Encoding encoding) {
        if (encoding == Encoding.HEX) {
            return HexUtil.encodeHexStr(data);
        }
        return Base64.getEncoder().encodeToString(data);
    }

    /**
     * 解码
     */
    private static byte[] decode(String data, Encoding encoding) {
        if (encoding == Encoding.HEX) {
            return HexUtil.decodeHex(data);
        }
        return Base64.getDecoder().decode(data);
    }
}
