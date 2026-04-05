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

package cn.xjbpm.rule.engine.definition.validator;

import cn.xjbpm.rule.common.utils.StringUtils;
import cn.xjbpm.rule.engine.definition.model.nodes.WxPayNode;
import cn.xjbpm.rule.engine.definition.model.nodes.WxPayNode.OperationType;
import org.springframework.util.Assert;

/**
 * 微信支付节点验证器
 *
 * @author 黄川 huchuc@vip.qq.com
 */
public class WxPayNodeValidator extends BaseNodeValidator<WxPayNode> {

    @Override
    public void check(WxPayNode node) {
        super.check(node);
        Assert.isTrue(StringUtils.isNotBlank(node.getCredentialId()), 
            "微信支付凭据不能为空");
        Assert.notNull(node.getOperationType(), 
            "操作类型不能为空");
        
        OperationType opType = node.getOperationType();
        
        switch (opType) {
            case NATIVE_PAY:
                Assert.isTrue(StringUtils.isNotBlank(node.getOrderNo()), 
                    "Native支付时订单号不能为空");
                Assert.notNull(node.getTotalAmount(), 
                    "Native支付时订单金额不能为空");
                break;
            case REFUND:
                Assert.isTrue(StringUtils.isNotBlank(node.getOrderNo()), 
                    "退款时订单号不能为空");
                Assert.isTrue(StringUtils.isNotBlank(node.getRefundNo()), 
                    "退款时退款单号不能为空");
                Assert.notNull(node.getOriginTotalAmount(), 
                    "退款时原订单金额不能为空");
                Assert.notNull(node.getRefundAmount(), 
                    "退款时退款金额不能为空");
                break;
            case QUERY_PAY_STATUS:
                Assert.isTrue(StringUtils.isNotBlank(node.getOrderNo()), 
                    "查询支付状态时订单号不能为空");
                break;
            case QUERY_REFUND_STATUS:
                Assert.isTrue(StringUtils.isNotBlank(node.getRefundNo()), 
                    "查询退款状态时退款单号不能为空");
                break;
            case DOWNLOAD_TRADE_BILL:
            case DOWNLOAD_FUND_FLOW_BILL:
                Assert.isTrue(StringUtils.isNotBlank(node.getBillDate()), 
                    "下载账单时账单日期不能为空");
                Assert.isTrue(StringUtils.isNotBlank(node.getBillDownloadDir()), 
                    "下载账单时账单下载目录不能为空");
                break;
            default:
                break;
        }
    }
}
