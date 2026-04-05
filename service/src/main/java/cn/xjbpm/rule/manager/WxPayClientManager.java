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

package cn.xjbpm.rule.manager;

import cn.xjbpm.rule.engine.runtime.model.credentials.WxPayCredential;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalNotification;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.http.DefaultHttpClientBuilder;
import com.wechat.pay.java.core.http.HttpClient;
import com.wechat.pay.java.service.billdownload.BillDownloadService;
import com.wechat.pay.java.service.partnerpayments.jsapi.JsapiService;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.refund.RefundService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 微信支付客户端缓存管理器
 * 使用Guava Cache按凭据ID缓存客户端实例
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Component
@Slf4j
public class WxPayClientManager {

    /**
     * 客户端缓存
     * key: credentialId
     * value: WxPayClientWrapper客户端包装实例
     */
    private final Cache<String, WxPayClientWrapper> clientCache = CacheBuilder.newBuilder()
            .maximumSize(50)
            .expireAfterAccess(1, TimeUnit.HOURS)
            .removalListener((RemovalNotification<String, WxPayClientWrapper> notification) -> {
                log.info("微信支付客户端缓存清理: credentialId={}, 原因={}",
                        notification.getKey(), notification.getCause());
            })
            .build();

    /**
     * 初始化客户端（带缓存）
     *
     * @param credentialId 凭据ID
     * @param credential   微信支付凭据
     */
    public void initClient(String credentialId, WxPayCredential credential) {
        try {
            WxPayClientWrapper wrapper = clientCache.get(credentialId, () -> {
                log.info("创建新的微信支付客户端实例: credentialId={}", credentialId);
                return createClient(credential);
            });
            credential.setNativePayService(wrapper.nativePayService);
            credential.setJsapiService(wrapper.jsapiService);
            credential.setRefundService(wrapper.refundService);
            credential.setBillDownloadService(wrapper.billDownloadService);
            credential.setHttpClient(wrapper.httpClient);
            credential.setConfig(wrapper.config);
        } catch (Exception e) {
            log.error("获取微信支付客户端失败: credentialId={}, error={}",
                    credentialId, e.getMessage(), e);
            throw new RuntimeException("获取微信支付客户端失败: " + e.getMessage(), e);
        }
    }

    /**
     * 创建客户端
     */
    private WxPayClientWrapper createClient(WxPayCredential wxPayCredential) {
        Config config = new RSAAutoCertificateConfig.Builder()
                .merchantId(wxPayCredential.getMerchantId())
                .privateKey(wxPayCredential.getPrivateKey())
                .merchantSerialNumber(wxPayCredential.getMerchantSerialNumber())
                .apiV3Key(wxPayCredential.getApiV3Key())
                .build();
        WxPayClientWrapper wrapper = new WxPayClientWrapper();
        wrapper.httpClient = new DefaultHttpClientBuilder().config(config).build();
        wrapper.nativePayService = new NativePayService.Builder().config(config).build();
        wrapper.jsapiService = new JsapiService.Builder().config(config).build();
        wrapper.refundService = new RefundService.Builder().config(config).build();
        wrapper.billDownloadService = new BillDownloadService.Builder().config(config).build();
        wrapper.config = config;
        return wrapper;
    }



    /**
     * 客户端包装类
     */
    private static class WxPayClientWrapper {
        private HttpClient httpClient;
        private NativePayService nativePayService;
        private JsapiService jsapiService;
        private RefundService refundService;
        private BillDownloadService billDownloadService;
        private  Config config;
    }
}
