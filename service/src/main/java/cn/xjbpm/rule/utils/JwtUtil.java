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

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * JWT 工具类，负责签发和验证 Token
 *
 * @author 黄川 huchuc@vip.qq.com
 */
public class JwtUtil {

    /**
     * 过期时间：24小时 (单位：毫秒)
     */
    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000;

    /**
     * 生成 JWT Token
     *
     * @param claims 自定义载荷内容
     * @param secret 签名密钥
     * @return 签名的 JWT 字符串
     */
    public static String createToken(Map<String, Object> claims, String secret) {
        return Jwts.builder()
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(secret))
                .compact();
    }

    /**
     * 根据字符串密钥生成安全密钥对象
     */
    private static SecretKey getSigningKey(String secret) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 验证 Token 是否有效
     *
     * @param token  JWT 字符串
     * @param secret 签名密钥
     * @return true 为有效，false 为无效
     */
    public static boolean validateToken(String token, String secret) {
        try {
            parseToken(token, secret);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 解析并校验 Token
     *
     * @param token  JWT 字符串
     * @param secret 签名密钥
     * @return 解析出的 Claims，如果无效则抛出异常
     */
    public static Claims parseToken(String token, String secret) {
        return Jwts.parser()
                .verifyWith(getSigningKey(secret))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}