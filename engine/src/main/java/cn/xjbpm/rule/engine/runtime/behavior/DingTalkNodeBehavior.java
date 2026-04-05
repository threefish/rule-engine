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

import cn.xjbpm.rule.common.utils.StringUtils;
import cn.xjbpm.rule.engine.aviator.AviatorContext;
import cn.xjbpm.rule.engine.aviator.AviatorExecutor;
import cn.xjbpm.rule.engine.definition.model.nodes.DingTalkNode;
import cn.xjbpm.rule.engine.runtime.model.FlowContext;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiRobotSendRequest;
import com.dingtalk.api.response.OapiRobotSendResponse;
import com.taobao.api.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 钉钉消息节点行为处理器
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Slf4j
public class DingTalkNodeBehavior implements NodeBehavior {

    private final DingTalkNode node;

    public DingTalkNodeBehavior(DingTalkNode node) {
        this.node = node;
    }

    @Override
    public void execution(FlowContext context) throws Exception {
        log.info("执行钉钉消息节点: {}", node.getName());
        DingTalkExecutionContext execContext = resolveExpressions(context);
        String result = sendDingTalkMessage(execContext);
        Map<String, Object> resultMap = buildResultMap(result, execContext);
        context.setNodeOutput(node.getId(), resultMap);
        if (context.isDebugModel()) {
            context.addTraceLog(node.getId(), "DingTalk result: {}", result);
        }
    }

    /**
     * 解析所有表达式
     */
    private DingTalkExecutionContext resolveExpressions(FlowContext context) {
        Map<String, Object> variable = context.getVariable();
        return DingTalkExecutionContext.builder()
                .webhookUrl(evaluateString(node.getWebhookUrl(), variable))
                .messageContent(evaluateString(node.getMessageContent(), variable))
                .secret(evaluateString(node.getSecret(), variable))
                .at(evaluateString(node.getAt(), variable))
                .atAll(node.isAtAll())
                .build();
    }

    private String evaluateString(String expression, Map<String, Object> variable) {
        if (expression == null) {
            return null;
        }
        return AviatorExecutor.evaluateString(AviatorContext.create(expression, variable));
    }


    /**
     * 发送钉钉消息
     */
    private String sendDingTalkMessage(DingTalkExecutionContext execContext) throws Exception {
        Assert.hasText(execContext.messageContent, "消息内容不能为空");

        String webhookUrl = execContext.webhookUrl;
        String secret = execContext.secret;

        if (StringUtils.isNotBlank(secret)) {
            webhookUrl = buildSignedWebhookUrl(webhookUrl, secret);
        }

        return sendRobotMessage(webhookUrl, execContext);
    }


    /**
     * 构建带签名的Webhook URL
     */
    private String buildSignedWebhookUrl(String webhookUrl, String secret) throws Exception {
        long timestamp = System.currentTimeMillis();
        String stringToSign = timestamp + "\n" + secret;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
        String sign = URLEncoder.encode(Base64.getEncoder().encodeToString(signData), StandardCharsets.UTF_8);

        String separator = webhookUrl.contains("?") ? "&" : "?";
        return webhookUrl + separator + "timestamp=" + timestamp + "&sign=" + sign;
    }

    /**
     * 发送机器人消息
     */
    private String sendRobotMessage(String webhookUrl, DingTalkExecutionContext execContext) throws ApiException {
        DingTalkClient client = new DefaultDingTalkClient(webhookUrl);
        OapiRobotSendRequest request = new OapiRobotSendRequest();
        request.setMsgtype("text");

        OapiRobotSendRequest.Text text = new OapiRobotSendRequest.Text();
        text.setContent(execContext.messageContent);
        request.setText(text);

        OapiRobotSendRequest.At at = new OapiRobotSendRequest.At();
        if (StringUtils.isNotBlank(execContext.at)) {
            at.setAtUserIds(Arrays.asList(execContext.at.split(",")));
        }
        at.setIsAtAll(execContext.atAll);
        request.setAt(at);

        OapiRobotSendResponse response = client.execute(request);
        log.info("钉钉消息发送结果: {}", response.getBody());
        return response.getBody();
    }

    /**
     * 构建结果映射
     */
    private Map<String, Object> buildResultMap(String result, DingTalkExecutionContext execContext) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put(RESULT, result);
        resultMap.put("messageContent", execContext.messageContent);
        return resultMap;
    }

    /**
     * 钉钉执行上下文
     */
    @lombok.Builder
    @lombok.Data
    private static class DingTalkExecutionContext {
        private String webhookUrl;
        private String messageContent;
        private String secret;
        private String at;
        private boolean atAll;
    }
}
