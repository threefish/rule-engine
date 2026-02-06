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


package cn.xjbpm.rule.common.utils.http;

import cn.xjbpm.rule.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 */
@Slf4j
public final class RestTemplateFactory {


    /**
     * 最大缓存容量
     */
    private static final int MAX_CACHE_SIZE = 100;

    /**
     * 使用线程安全的 LinkedHashMap 实现 LRU 缓存
     */
    private static final Map<String, RestTemplate> CACHE = Collections.synchronizedMap(
            new LinkedHashMap<>(MAX_CACHE_SIZE, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, RestTemplate> eldest) {
                    return size() > MAX_CACHE_SIZE;
                }
            }
    );

    private RestTemplateFactory() {
    }

    public static RestTemplate getRestTemplate() {
        return getRestTemplate(TimeoutOptions.DEFAULT);
    }

    /**
     * 获取 RestTemplate
     */
    public static RestTemplate getRestTemplate(TimeoutOptions options) {
        // 缓存键包含超时设置和代理完整地址
        String key = String.format("%d_%d_%s",
                options.getConnectTimeout(),
                options.getReadTimeout(),
                options.getProxyHost() != null ? options.getProxyHost() : "none");

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
                connection.setInstanceFollowRedirects(false);

                if (connection instanceof javax.net.ssl.HttpsURLConnection https) {
                    https.setSSLSocketFactory(createIgnoreSslSocketFactory());
                    https.setHostnameVerifier((hostname, session) -> true);
                }
            }
        };

        // --- 适配包含端口的代理地址 ---
        String proxyAddr = options.getProxyHost();
        if (StringUtils.isNotBlank(proxyAddr)) {
            try {
                String[] split = proxyAddr.split("\\:");
                String type = split[0];
                String host = split[1];
                int port = Integer.valueOf(split[2]);
                factory.setProxy(new Proxy(Proxy.Type.valueOf(type), new InetSocketAddress(host, port)));
            } catch (Exception e) {
                log.error("代理地址格式错误", e);
            }
        }

        factory.setConnectTimeout(options.getConnectTimeout());
        factory.setReadTimeout(options.getReadTimeout());

        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public boolean hasError(org.springframework.http.client.ClientHttpResponse response) {
                return false;
            }
        });

        return restTemplate;
    }

    private static javax.net.ssl.SSLSocketFactory createIgnoreSslSocketFactory() {
        try {
            javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[]{
                    new javax.net.ssl.X509TrustManager() {
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[0];
                        }
                    }
            };
            javax.net.ssl.SSLContext sslContext = javax.net.ssl.SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create SSL context", e);
        }
    }
}