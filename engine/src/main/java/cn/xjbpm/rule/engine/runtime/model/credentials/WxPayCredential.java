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
package cn.xjbpm.rule.engine.runtime.model.credentials;

import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.http.HttpClient;
import com.wechat.pay.java.service.billdownload.BillDownloadService;
import com.wechat.pay.java.service.partnerpayments.jsapi.JsapiService;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.refund.RefundService;
import lombok.Data;

/**
 * 微信支付凭据
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Data
public class WxPayCredential {

    /**
     * 商户号
     */
    private String merchantId;
    /**
     * 商户API私钥
     */
    private String privateKey;
    /**
     * 商户证书序列号
     */
    private String merchantSerialNumber;
    /**
     * APIv3密钥
     */
    private String apiV3Key;
    /**
     * APPID
     */
    private String appId;
    /**
     * 支付回调URL
     */
    private String notifyPayUrl;
    /**
     * 退款回调URL
     */
    private String notifyRefundUrl;
    /**
     * 货币
     */
    private String currency = "CNY";
    /**
     * 客户端
     */
    private NativePayService nativePayService;
    /**
     * JSAPI服务客户端
     */
    private JsapiService jsapiService;

    /**
     * 退款服务客户端
     */
    private RefundService refundService;
    /**
     * 账单服务客户端
     */
    private BillDownloadService billDownloadService;
    /**
     * HTTP客户端
     */
    private HttpClient httpClient;
    /**
     * 微信支付配置
     */
    private Config config;


}
