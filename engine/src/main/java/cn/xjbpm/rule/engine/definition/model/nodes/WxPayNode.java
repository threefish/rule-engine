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

package cn.xjbpm.rule.engine.definition.model.nodes;

import cn.xjbpm.rule.engine.definition.model.enums.NodeType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.stream.Stream;

/**
 * 微信支付节点
 * 支持Native支付、退款、查询支付状态、查询退款状态、下载账单等操作
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WxPayNode extends Node {

    /**
     * 凭据ID
     */
    private String credentialId;

    /**
     * 操作类型
     */
    private OperationType operationType;

    /**
     * 订单号（支付/退款/查询时需要）
     */
    private String orderNo;
    /**
     * 用户在商户下唯一的OpenId
     */
    private String spOpenId;

    /**
     * 退款单号（退款时需要）
     */
    private String refundNo;

    /**
     * 订单金额（分）
     */
    private String totalAmount;
    /**
     * 过期时间（小时）
     */
    private String timeExpireHours;

    /**
     * 原订单金额（退款时需要，分）
     */
    private String originTotalAmount;

    /**
     * 退款金额（分）
     */
    private String refundAmount;

    /**
     * 订单描述
     */
    private String orderInfo;

    /**
     * 退款原因
     */
    private String refundReason;

    /**
     * 账单日期（下载账单时需要，格式：yyyy-MM-dd）
     */
    private String billDate;

    /**
     * 账单下载目录
     */
    private String billDownloadDir;

    @Override
    public NodeType getType() {
        return NodeType.WxPayNode;
    }

    /**
     * 操作类型枚举
     */
    @AllArgsConstructor
    public static enum OperationType {
        NATIVE_PAY("native_pay", "创建Native支付单"),
        JS_PAY("js_pay", "创建小程序支付单"),
        REFUND("refund", "创建退款单"),
        QUERY_PAY_STATUS("query_pay_status", "查询支付状态"),
        QUERY_REFUND_STATUS("query_refund_status", "查询退款状态"),
        DOWNLOAD_TRADE_BILL("download_trade_bill", "下载交易账单"),
        DOWNLOAD_FUND_FLOW_BILL("download_fund_flow_bill", "下载资金账单"),
        SIGN_VERIFY("sign_verify", "支付回调签名校验"),
        ;

        @JsonValue
        private final String value;
        private final String description;

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public static OperationType fromValue(String value) {
            return Stream.of(OperationType.values())
                    .filter(r -> r.value.equals(value))
                    .findFirst()
                    .orElse(NATIVE_PAY);
        }
    }
}
