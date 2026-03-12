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

package cn.xjbpm.rule.utils;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-GCM 加密工具类
 * 用于敏感数据的加密和解密
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Slf4j
public class AESCryptoUtil {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    private AESCryptoUtil() {
    }

    /**
     * 使用 AES-GCM 加密字符串
     *
     * @param plainText 明文
     * @param secretKey 密钥（必须为16/24/32字节）
     * @return Base64编码的密文（包含IV）
     */
    public static String encrypt(String plainText, String secretKey) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        try {
            byte[] keyBytes = validateAndGetKey(secretKey);
            byte[] iv = generateIV();

            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);

            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            log.error("AES加密失败: {}", e.getMessage(), e);
            throw new RuntimeException("加密失败", e);
        }
    }

    /**
     * 使用 AES-GCM 解密字符串
     *
     * @param encryptedText Base64编码的密文（包含IV）
     * @param secretKey     密钥（必须为16/24/32字节）
     * @return 明文
     */
    public static String decrypt(String encryptedText, String secretKey) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }
        try {
            byte[] keyBytes = validateAndGetKey(secretKey);
            byte[] combined = Base64.getDecoder().decode(encryptedText);

            if (combined.length < GCM_IV_LENGTH) {
                throw new IllegalArgumentException("密文格式无效");
            }

            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] encrypted = new byte[combined.length - GCM_IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH);
            System.arraycopy(combined, GCM_IV_LENGTH, encrypted, 0, encrypted.length);

            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);

            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("AES解密失败: {}", e.getMessage(), e);
            throw new RuntimeException("解密失败", e);
        }
    }

    /**
     * 生成随机IV
     */
    private static byte[] generateIV() {
        byte[] iv = new byte[GCM_IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        return iv;
    }

    /**
     * 验证并获取密钥字节数组
     * 支持 AES-128/192/256
     */
    private static byte[] validateAndGetKey(String secretKey) {
        if (secretKey == null || secretKey.isEmpty()) {
            throw new IllegalArgumentException("密钥不能为空");
        }

        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        int keyLength = keyBytes.length;

        if (keyLength == 16 || keyLength == 24 || keyLength == 32) {
            return keyBytes;
        }

        if (keyLength < 16) {
            byte[] paddedKey = new byte[16];
            System.arraycopy(keyBytes, 0, paddedKey, 0, keyLength);
            return paddedKey;
        }

        if (keyLength < 24) {
            byte[] paddedKey = new byte[24];
            System.arraycopy(keyBytes, 0, paddedKey, 0, keyLength);
            return paddedKey;
        }

        byte[] truncatedKey = new byte[32];
        System.arraycopy(keyBytes, 0, truncatedKey, 0, 32);
        return truncatedKey;
    }

    /**
     * 生成随机密钥
     *
     * @param keySize 密钥长度（16/24/32字节）
     * @return Base64编码的密钥
     */
    public static String generateKey(int keySize) {
        if (keySize != 16 && keySize != 24 && keySize != 32) {
            throw new IllegalArgumentException("密钥长度必须为16、24或32字节");
        }
        byte[] key = new byte[keySize];
        SecureRandom random = new SecureRandom();
        random.nextBytes(key);
        return Base64.getEncoder().encodeToString(key);
    }
}
