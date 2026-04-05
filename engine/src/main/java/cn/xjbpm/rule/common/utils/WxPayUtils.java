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
package cn.xjbpm.rule.common.utils;

import cn.xjbpm.rule.engine.runtime.model.credentials.WxPayCredential;
import com.wechat.pay.java.service.billdownload.model.GetFundFlowBillRequest;
import com.wechat.pay.java.service.billdownload.model.GetTradeBillRequest;
import com.wechat.pay.java.service.billdownload.model.QueryBillEntity;
import com.wechat.pay.java.service.partnerpayments.jsapi.model.Payer;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.payments.nativepay.model.Amount;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse;
import com.wechat.pay.java.service.payments.nativepay.model.QueryOrderByOutTradeNoRequest;
import com.wechat.pay.java.service.refund.model.AmountReq;
import com.wechat.pay.java.service.refund.model.CreateRequest;
import com.wechat.pay.java.service.refund.model.QueryByOutRefundNoRequest;
import com.wechat.pay.java.service.refund.model.Refund;
import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @author 黄川 huchuc@vip.qq.com
 */
public class WxPayUtils {


    /**
     * 微信扫码支付
     *
     * @param orderNo
     * @param total
     * @param orderInfo
     * @param timeExpireHours
     * @param credential
     * @return
     */
    public static String nativePay(String orderNo, Integer total, String orderInfo, Integer timeExpireHours, WxPayCredential credential) {
        Assert.isTrue(StringUtils.isNotBlank(orderNo), "订单号不能为空");
        Assert.isTrue(StringUtils.isNotBlank(orderInfo), "订单描述不能为空");
        Assert.notNull(total, "订单金额不能为空");
        Assert.isTrue(total > 0, "订单金额必须大于0");
        PrepayRequest request = new PrepayRequest();
        Amount amount = new Amount();
        amount.setTotal(total);
        amount.setCurrency(credential.getCurrency());
        request.setTimeExpire(LocalDateTime.now().plusHours(timeExpireHours)
                .atZone(ZoneId.of("Asia/Shanghai"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")));
        request.setAmount(amount);
        request.setAppid(credential.getAppId());
        request.setMchid(credential.getMerchantId());
        request.setNotifyUrl(credential.getNotifyPayUrl());
        request.setOutTradeNo(orderNo);
        request.setDescription(orderInfo);
        PrepayResponse response = credential.getNativePayService().prepay(request);
        // 使用微信扫描 code_url 对应的二维码，即可体验原生支付
        return response.getCodeUrl();
    }

    /**
     * 微信JSPAi支付
     *
     * @param orderNo
     * @param total
     * @param spOpenId
     * @param orderInfo
     * @param timeExpireHours
     * @param credential
     * @return
     */
    public static String jsPay(String orderNo, Integer total, String spOpenId, String orderInfo, Integer timeExpireHours, WxPayCredential credential) {
        Assert.isTrue(StringUtils.isNotBlank(orderNo), "订单号不能为空");
        Assert.isTrue(StringUtils.isNotBlank(orderInfo), "订单描述不能为空");
        Assert.isTrue(StringUtils.isNotBlank(spOpenId), "用户openId不能为空");
        Assert.notNull(total, "订单金额不能为空");
        Assert.isTrue(total > 0, "订单金额必须大于0");
        com.wechat.pay.java.service.partnerpayments.jsapi.model.PrepayRequest request = new com.wechat.pay.java.service.partnerpayments.jsapi.model.PrepayRequest();
        com.wechat.pay.java.service.partnerpayments.jsapi.model.Amount amount = new com.wechat.pay.java.service.partnerpayments.jsapi.model.Amount();
        amount.setTotal(total);
        amount.setCurrency(credential.getCurrency());
        request.setTimeExpire(LocalDateTime.now().plusHours(timeExpireHours)
                .atZone(ZoneId.of("Asia/Shanghai"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")));
        request.setAmount(amount);
        request.setNotifyUrl(credential.getNotifyPayUrl());
        request.setOutTradeNo(orderNo);
        request.setDescription(orderInfo);
        Payer payer = new Payer();
        payer.setSpOpenid(spOpenId);
        request.setPayer(payer);
        com.wechat.pay.java.service.partnerpayments.jsapi.model.PrepayResponse prepay = credential.getJsapiService().prepay(request);
        return prepay.getPrepayId();
    }

    /**
     * 微信退款
     *
     * @param orderNo
     * @param refundNo
     * @param originTotal
     * @param refundTotal
     * @param reason
     * @param credential
     * @return
     */
    public static Refund refund(String orderNo, String refundNo, Integer originTotal, Integer refundTotal, String reason, WxPayCredential credential) {
        CreateRequest request = new CreateRequest();
        request.setOutTradeNo(orderNo);// 订单编号
        request.setOutRefundNo(refundNo);// 退款单编号
        request.setReason(reason);
        request.setNotifyUrl(credential.getNotifyRefundUrl());
        AmountReq amount = new AmountReq();
        // 退款金额
        amount.setRefund(originTotal.longValue());
        // 原订单金额
        amount.setTotal(refundTotal.longValue());
        // 退款币种
        amount.setCurrency(credential.getCurrency());
        request.setAmount(amount);
        return credential.getRefundService().create(request);

    }

    /**
     * 查询订单支付状态
     *
     * @param orderNo
     * @param credential
     * @return
     */
    public static Transaction queryOrderPayStatus(String orderNo, WxPayCredential credential) {
        QueryOrderByOutTradeNoRequest request = new QueryOrderByOutTradeNoRequest();
        request.setMchid(credential.getMerchantId());
        request.setOutTradeNo(orderNo);
        return credential.getNativePayService().queryOrderByOutTradeNo(request);
    }

    /**
     * 查询订单退款状态
     *
     * @param refundNo
     * @param credential
     * @return
     */
    public static Refund queryOrderRefundStatus(String refundNo, WxPayCredential credential) {
        QueryByOutRefundNoRequest request = new QueryByOutRefundNoRequest();
        request.setOutRefundNo(refundNo);
        return credential.getRefundService().queryByOutRefundNo(request);
    }

    /**
     * 下载交易账单
     *
     * @param billDate
     * @param directory
     * @param credential
     * @return
     */
    public static File downloadTradeBill(String billDate, String directory, WxPayCredential credential) {
        GetTradeBillRequest request = new GetTradeBillRequest();
        request.setBillDate(billDate);
        QueryBillEntity tradeBill = credential.getBillDownloadService().getTradeBill(request);
        String downloadUrl = tradeBill.getDownloadUrl();
        return downloadBill(downloadUrl, directory, credential);

    }

    /**
     * 下载资金账单
     *
     * @param billDate
     * @param directory
     * @param credential
     * @return
     */
    public static File downloadFundFlowBill(String billDate, String directory, WxPayCredential credential) {
        GetFundFlowBillRequest request = new GetFundFlowBillRequest();
        request.setBillDate(billDate);
        QueryBillEntity tradeBill = credential.getBillDownloadService().getFundFlowBill(request);
        String downloadUrl = tradeBill.getDownloadUrl();
        return downloadBill(downloadUrl, directory, credential);
    }

    /**
     * 下载账单
     *
     * @param downloadUrl
     * @param directory
     * @param credential
     * @return
     */
    private static File downloadBill(String downloadUrl, String directory, WxPayCredential credential) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date());
        String fileName = "wx_bill_" + timeStamp + ".xls";
        File dir = new File(directory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File targetFile = new File(dir, fileName);
        // 3. 执行下载与保存
        try (InputStream inputStream = credential.getHttpClient().download(downloadUrl)) {
            // 直接将流拷贝到目标路径
            Files.copy(inputStream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return targetFile;
        } catch (IOException e) {
            throw new RuntimeException("写入账单文件失败: " + directory, e);
        }
    }
}