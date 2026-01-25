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
package cn.xjbpm.rule.common.utils.http;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 黄川 huchuc@vip.qq.com
 */
public final class RestTemplateFactory {

    /**
     * 按 timeout 缓存 RestTemplate
     * key = connectTimeout + "_" + readTimeout
     */
    private static final Map<String, RestTemplate> CACHE =
            new ConcurrentHashMap<>();

    private RestTemplateFactory() {
    }

    /**
     * 获取 RestTemplate（使用默认超时）
     */
    public static RestTemplate getRestTemplate() {
        return getRestTemplate(TimeoutOptions.DEFAULT);
    }

    /**
     * 获取 RestTemplate（指定超时）
     */
    public static RestTemplate getRestTemplate(TimeoutOptions options) {
        String key = options.getConnectTimeout() + "_" + options.getReadTimeout();
        return CACHE.computeIfAbsent(key, k -> create(options));
    }

    /**
     * 创建 RestTemplate
     */
    private static RestTemplate create(TimeoutOptions options) {

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory() {

            @Override
            protected void prepareConnection(HttpURLConnection connection, String httpMethod)
                    throws IOException {

                super.prepareConnection(connection, httpMethod);
                // 禁止重定向
                connection.setInstanceFollowRedirects(false);
                javax.net.ssl.HttpsURLConnection https = (javax.net.ssl.HttpsURLConnection) connection;
                https.setSSLSocketFactory(createIgnoreSslSocketFactory());
                https.setHostnameVerifier((hostname, session) -> true);
            }
        };

        factory.setConnectTimeout(options.getConnectTimeout());
        factory.setReadTimeout(options.getReadTimeout());
        RestTemplate restTemplate = new RestTemplate(factory);

        // 不在 RestTemplate 层抛异常，交给调用方处理
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public boolean hasError(org.springframework.http.client.ClientHttpResponse response) {
                return false;
            }
        });

        return restTemplate;
    }


    /**
     * 创建忽略 SSL 校验的 SSLSocketFactory
     */
    private static javax.net.ssl.SSLSocketFactory createIgnoreSslSocketFactory() {
        try {
            javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[]{
                    new javax.net.ssl.X509TrustManager() {
                        public void checkClientTrusted(
                                java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        public void checkServerTrusted(
                                java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[0];
                        }
                    }
            };

            javax.net.ssl.SSLContext sslContext =
                    javax.net.ssl.SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            return sslContext.getSocketFactory();

        } catch (Exception e) {
            throw new IllegalStateException("Failed to create SSL context", e);
        }
    }
}
