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

import cn.hutool.core.collection.CollUtil;
import cn.xjbpm.rule.common.utils.HttpHeaderUtils;
import cn.xjbpm.rule.common.utils.StringUtils;
import cn.xjbpm.rule.engine.aviator.functions.UrlUtils;
import jakarta.validation.constraints.NotNull;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiConsumer;

/**
 * @author 黄川 huchuc@vip.qq.com
 */
public final class HttpCallUtil {

    private HttpCallUtil() {
    }

    public static HttpCallResult execute(
            HttpMethod method,
            String url,
            Object body,
            Map<String, String> headers,
            Map<String, ?> uriVariables,
            TimeoutOptions timeout) {

        try {
            RestTemplate restTemplate = getRestTemplate(timeout);

            HttpHeaders httpHeaders = buildHeaders(headers);

            HttpEntity<?> entity =
                    body == null ? new HttpEntity<>(httpHeaders)
                                 : new HttpEntity<>(body, httpHeaders);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    method,
                    entity,
                    String.class,
                    uriVariables == null ? Collections.emptyMap() : uriVariables
            );

            return toResult(response);

        } catch (ResourceAccessException e) {
            throw new HttpCallException(method.name(), url,
                    "HTTP " + method.name() + " timeout or connection error", e);
        } catch (RestClientException e) {
            throw new HttpCallException(method.name(), url,
                    "HTTP " + method.name() + " failed", e);
        }
    }

    private static RestTemplate getRestTemplate(TimeoutOptions timeout) {
        return timeout == null
               ? RestTemplateFactory.getRestTemplate()
               : RestTemplateFactory.getRestTemplate(timeout);
    }

    private static HttpHeaders buildHeaders(Map<String, String> headers) {
        HttpHeaders httpHeaders = new HttpHeaders();
        if (headers != null) {
            headers.forEach(httpHeaders::add);
        }
        return httpHeaders;
    }

    private static HttpCallResult toResult(ResponseEntity<String> response) {
        return new HttpCallResult(
                response.getStatusCodeValue(),
                response.getBody(),
                Objects.nonNull(response.getHeaders()) ? HttpHeaderUtils.toCamelCaseMap(response.getHeaders()) : Collections.emptyMap()
        );
    }

    /**
     * 流式下载执行器
     * 通过回调函数处理 InputStream，可以将数据直接写入 FileOutputStream 或 HttpServletResponse，从而避免 OOM。
     *
     * @param streamHandler 两个参数分别为：响应输入流、响应对象(包含状态码和头信息)
     */
    public static HttpCallResult executeStreaming(
            HttpMethod method,
            String url,
            String body,
            Map<String, String> headers,
            Map<String, String> uriVariables,
            TimeoutOptions timeout,
            @NotNull BiConsumer<InputStream, ResponseEntity<Resource>> streamHandler) {
        try {
            RestTemplate restTemplate = getRestTemplate(timeout);
            HttpHeaders httpHeaders = buildHeaders(headers);
            HttpEntity<?> requestEntity = new HttpEntity<>(httpHeaders);
            URI uri = URI.create(url);
            List<String> params = new ArrayList<>();
            if (CollUtil.isNotEmpty(uriVariables)) {
                for (Map.Entry<String, String> entry : uriVariables.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    String encode = UrlUtils.encode(value);
                    params.add(String.format("%s=%s", key, encode));
                }
                if (StringUtils.isNotBlank(uri.getRawQuery())) {
                    uri = URI.create(url + "&" + String.join("&", params));
                } else {
                    uri = URI.create(url + "?" + String.join("&", params));
                }
            } else {
                uri = URI.create(url);
            }
            if (StringUtils.isNotBlank(body)) {
                if (method == HttpMethod.POST && httpHeaders.getContentType() == MediaType.MULTIPART_FORM_DATA) {
                    httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                }
                requestEntity = new HttpEntity<>(body, httpHeaders);
            }
            ResponseEntity<Resource> response = restTemplate.exchange(uri, method, requestEntity, Resource.class);
            Map<String, Object> resultHeaders = HttpHeaderUtils.toCamelCaseMap(response.getHeaders());
            int statusCode = response.getStatusCode().value();
            try (InputStream inputStream = response.getBody().getInputStream()) {
                if (response.getStatusCode() == HttpStatus.OK) {
                    streamHandler.accept(inputStream, response);
                    return new HttpCallResult(statusCode, null, resultHeaders);
                }
                byte[] bytes = StreamUtils.copyToByteArray(inputStream);
                return new HttpCallResult(statusCode, new String(bytes, StandardCharsets.UTF_8), resultHeaders);
            }
        } catch (ResourceAccessException e) {
            throw new HttpCallException(method.name(), url, "HTTP 连接或超时异常", e);
        } catch (RestClientException e) {
            throw new HttpCallException(method.name(), url, "HTTP 请求失败", e);
        } catch (Exception e) {
            throw new HttpCallException(method.name(), url, "执行流式请求发生未知错误", e);
        }
    }
}
