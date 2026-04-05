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

package cn.xjbpm.rule.engine.runtime.behavior;

import cn.xjbpm.rule.common.utils.JsonPathUtil;
import cn.xjbpm.rule.common.utils.WxPayUtils;
import cn.xjbpm.rule.engine.aviator.AviatorContext;
import cn.xjbpm.rule.engine.aviator.AviatorExecutor;
import cn.xjbpm.rule.engine.definition.model.nodes.WxPayNode;
import cn.xjbpm.rule.engine.definition.model.nodes.WxPayNode.OperationType;
import cn.xjbpm.rule.engine.runtime.behavior.model.FileModel;
import cn.xjbpm.rule.engine.runtime.model.FlowContext;
import cn.xjbpm.rule.engine.runtime.model.credentials.WxPayCredential;
import com.jayway.jsonpath.DocumentContext;
import com.wechat.pay.java.core.exception.ValidationException;
import com.wechat.pay.java.core.notification.NotificationConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.refund.model.Refund;
import com.wechat.pay.java.service.refund.model.RefundNotification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 微信支付节点行为处理器
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Slf4j
public class WxPayNodeBehavior implements NodeBehavior {

    private final WxPayNode node;

    public WxPayNodeBehavior(WxPayNode node) {
        this.node = node;
    }

    @Override
    public void execution(FlowContext context) throws Exception {
        WxPayCredential credential = context.getEngineServices()
                .getCredentialsManager()
                .getWxPayCredential(node.getCredentialId());

        log.info("执行微信支付节点: {}", node.getName());

        Assert.notNull(credential, "微信支付凭据不能为空");

        WxPayExecutionContext execContext = resolveExpressions(context);
        this.executeOperation(context, credential, execContext);
        if (context.isDebugModel()) {
            context.addTraceLog(node.getId(), "微信支付操作完成: {}", execContext.operationType);
        }
    }

    /**
     * 解析表达式
     */
    private WxPayExecutionContext resolveExpressions(FlowContext context) {
        Map<String, Object> variable = context.getVariable();
        return WxPayExecutionContext.builder()
                .operationType(node.getOperationType())
                .orderNo(evaluateString(node.getOrderNo(), variable))
                .refundNo(evaluateString(node.getRefundNo(), variable))
                .spOpenId(evaluateString(node.getSpOpenId(), variable))
                .totalAmount(evaluateInteger(node.getTotalAmount(), variable))
                .timeExpireHours(evaluateInteger(node.getTimeExpireHours(), variable))
                .originTotalAmount(evaluateInteger(node.getOriginTotalAmount(), variable))
                .refundAmount(evaluateInteger(node.getRefundAmount(), variable))
                .orderInfo(evaluateString(node.getOrderInfo(), variable))
                .refundReason(evaluateString(node.getRefundReason(), variable))
                .billDate(evaluateString(node.getBillDate(), variable))
                .billDownloadDir(evaluateString(node.getBillDownloadDir(), variable))
                .build();
    }

    /**
     * 执行支付操作
     */
    private Map<String, Object> executeOperation(FlowContext context, WxPayCredential credential, WxPayExecutionContext ctx) {
        Map<String, Object> result = new HashMap<>();
        result.put("operationType", ctx.operationType);
        try {
            switch (ctx.operationType) {
                case NATIVE_PAY:
                    executeNativePay(credential, ctx, result);
                    break;
                case JS_PAY:
                    executeJsPay(credential, ctx, result);
                    break;
                case REFUND:
                    executeRefund(credential, ctx, result);
                    break;
                case QUERY_PAY_STATUS:
                    executeQueryPayStatus(credential, ctx, result);
                    break;
                case QUERY_REFUND_STATUS:
                    executeQueryRefundStatus(credential, ctx, result);
                    break;
                case DOWNLOAD_TRADE_BILL:
                    executeDownloadTradeBill(credential, ctx, result);
                    break;
                case DOWNLOAD_FUND_FLOW_BILL:
                    executeDownloadFundFlowBill(credential, ctx, result);
                    break;
                case SIGN_VERIFY:
                    executeSignVerify(credential, ctx, context, result);
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown OperationType: " + ctx.operationType);
            }
            result.put("success", true);
        } catch (Exception e) {
            log.error("微信支付节点操作失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("errorMessage", e.getMessage());
            throw e;
        } finally {
            context.setNodeOutput(node.getId(), result);
        }
        return result;
    }

    /**
     * 执行Native支付
     */
    private void executeNativePay(WxPayCredential credential, WxPayExecutionContext ctx, Map<String, Object> result) {
        String codeUrl = WxPayUtils.nativePay(ctx.orderNo, ctx.totalAmount, ctx.orderInfo,ctx.timeExpireHours, credential);
        result.put("codeUrl", codeUrl);
        result.put("orderNo", ctx.orderNo);
        result.put("totalAmount", ctx.totalAmount);
    }

    /**
     * 执行JSAPI支付
     */
    private void executeJsPay(WxPayCredential credential, WxPayExecutionContext ctx, Map<String, Object> result) {
        String prepayId = WxPayUtils.jsPay(ctx.orderNo, ctx.totalAmount, ctx.spOpenId, ctx.orderInfo,ctx.timeExpireHours, credential);
        result.put("prepayId", prepayId);
        result.put("orderNo", ctx.orderNo);
        result.put("totalAmount", ctx.totalAmount);
    }

    /**
     * 执行退款
     */
    private void executeRefund(WxPayCredential credential, WxPayExecutionContext ctx, Map<String, Object> result) {
        Refund refund = WxPayUtils.refund(ctx.orderNo, ctx.refundNo, ctx.originTotalAmount, ctx.refundAmount, ctx.refundReason, credential);
        result.put("refund", refund);
        result.put("status", refund.getStatus());
        result.put("refundId", refund.getRefundId());
        result.put("originTotalAmount", ctx.originTotalAmount);
        result.put("refundAmount", ctx.refundAmount);
    }

    /**
     * 执行查询支付状态
     */
    private void executeQueryPayStatus(WxPayCredential credential, WxPayExecutionContext ctx, Map<String, Object> result) {
        Transaction transaction = WxPayUtils.queryOrderPayStatus(ctx.orderNo, credential);
        result.put("orderNo", ctx.orderNo);
        result.put("tradeState", transaction.getTradeState());
        result.put("tradeType", transaction.getTradeType());
        result.put("transaction", transaction);
    }

    /**
     * 执行查询退款状态
     */
    private void executeQueryRefundStatus(WxPayCredential credential, WxPayExecutionContext ctx, Map<String, Object> result) {
        Refund refund = WxPayUtils.queryOrderRefundStatus(ctx.refundNo, credential);
        result.put("refundNo", ctx.refundNo);
        result.put("status", refund.getStatus());
        result.put("refundId", refund.getRefundId());
        result.put("refund", refund);
    }

    /**
     * 执行下载交易账单
     */
    private void executeDownloadTradeBill(WxPayCredential credential, WxPayExecutionContext ctx, Map<String, Object> result) {
        File billFile = WxPayUtils.downloadTradeBill(ctx.billDate, ctx.billDownloadDir, credential);
        result.put("file", FileModel.of(billFile));
        result.put("billDate", ctx.billDate);
    }

    /**
     * 执行下载资金账单
     */
    private void executeDownloadFundFlowBill(WxPayCredential credential, WxPayExecutionContext ctx, Map<String, Object> result) {
        File billFile = WxPayUtils.downloadFundFlowBill(ctx.billDate, ctx.billDownloadDir, credential);
        result.put("file", FileModel.of(billFile));
        result.put("billDate", ctx.billDate);
    }

    /**
     * 执行签名验证
     */
    private void executeSignVerify(WxPayCredential credential, WxPayExecutionContext ctx, FlowContext context, Map<String, Object> result) {
        Map<String, Object> variables = context.getVariable();
        Object headerObject = variables.get("headers");
        Assert.notNull(headerObject, "headers can not be null");
        Assert.isInstanceOf(Map.class, headerObject, "headers must be Map");
        if (headerObject instanceof Map) {
            String body = String.valueOf(variables.get("body"));
            Map<String, String> headers = (Map<String, String>) headerObject;
            RequestParam requestParam = new RequestParam.Builder()
                    .serialNumber(headers.get("WechatpaySerial"))
                    .nonce(headers.get("WechatpayNonce"))
                    .signature(headers.get("WechatpaySignature"))
                    .timestamp(headers.get("WechatpayTimestamp"))
                    .body(body)
                    .build();
            try {
                DocumentContext doc = JsonPathUtil.parse(body);
                String type = doc.read("$.resource.original_type", String.class);
                NotificationParser parser = new NotificationParser((NotificationConfig) credential.getConfig());
                if (Objects.equals(type, "transaction")) {
                    Transaction parse = parser.parse(requestParam, Transaction.class);
                    result.put(RESULT, parse);
                }
                if (Objects.equals(type, "refund")) {
                    RefundNotification parse = parser.parse(requestParam, RefundNotification.class);
                    result.put(RESULT, parse);
                }
            } catch (ValidationException e) {
                result.put("errorMessage", e.getMessage());
                throw e;
            }
        }
    }

    private String evaluateString(String expression, Map<String, Object> variable) {
        if (expression == null) {
            return null;
        }
        return AviatorExecutor.evaluateString(AviatorContext.create(expression, variable));
    }

    private Integer evaluateInteger(String value, Map<String, Object> variable) {
        String result = AviatorExecutor.evaluateString(AviatorContext.create(value, variable));
        return Objects.nonNull(result) ? Integer.parseInt(result) : null;
    }

    @lombok.Builder
    @lombok.Data
    private static class WxPayExecutionContext {
        private OperationType operationType;
        private String orderNo;
        private String refundNo;
        private String spOpenId;
        private Integer totalAmount;
        private Integer originTotalAmount;
        private Integer refundAmount;
        private String orderInfo;
        private String refundReason;
        private String billDate;
        private String billDownloadDir;
        private Integer timeExpireHours;
    }
}
