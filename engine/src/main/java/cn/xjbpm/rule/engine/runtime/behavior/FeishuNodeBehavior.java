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

import cn.xjbpm.rule.common.constant.RuleFlowConstant;
import cn.xjbpm.rule.common.utils.JsonUtils;
import cn.xjbpm.rule.common.utils.StringUtils;
import cn.xjbpm.rule.engine.aviator.AviatorContext;
import cn.xjbpm.rule.engine.aviator.AviatorExecutor;
import cn.xjbpm.rule.engine.definition.model.nodes.FeishuNode;
import cn.xjbpm.rule.engine.runtime.model.FlowContext;
import cn.xjbpm.rule.engine.runtime.model.credentials.FeishuCredential;
import com.lark.oapi.Client;
import com.lark.oapi.service.im.v1.model.*;
import lombok.extern.slf4j.Slf4j;
import org.nutz.lang.random.R;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 飞书消息节点行为处理器
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Slf4j
public class FeishuNodeBehavior implements NodeBehavior {

    private final FeishuNode node;

    public FeishuNodeBehavior(FeishuNode node) {
        this.node = node;
    }

    @Override
    public void execution(FlowContext context) throws Exception {
        log.info("执行飞书消息节点: {}", node.getName());

        FeishuExecutionContext execContext = resolveExpressions(context);

        String result;
        if (execContext.operationType == FeishuNode.OperationType.REPLY) {
            result = replyFeishuMessage(context, execContext);
        } else {
            result = sendFeishuMessage(context, execContext);
        }

        Map<String, Object> resultMap = buildResultMap(result, execContext);
        context.setNodeOutput(node.getId(), resultMap);

        if (context.isDebugModel()) {
            context.addTraceLog(node.getId(), "Feishu result: {}", new Object[]{result});
        }
    }

    /**
     * 解析所有表达式
     */
    private FeishuExecutionContext resolveExpressions(FlowContext context) {
        Map<String, Object> variable = context.getVariable();
        return FeishuExecutionContext.builder()
                .credentialId(node.getCredentialId())
                .operationType(node.getOperationType())
                .receiverIdType(node.getReceiverIdType())
                .receiverId(evaluateString(node.getReceiverId(), variable))
                .messageId(evaluateString(node.getMessageId(), variable))
                .msgType(node.getMsgType())
                .messageContent(evaluateString(node.getMessageContent(), variable))
                .replyInThread(node.isReplyInThread())
                .build();
    }

    private String evaluateString(String expression, Map<String, Object> variable) {
        if (expression == null) {
            return null;
        }
        return AviatorExecutor.evaluateString(AviatorContext.create(expression, variable));
    }


    /**
     * 发送飞书消息
     */
    private String sendFeishuMessage(FlowContext context, FeishuExecutionContext execContext) throws Exception {
        Assert.hasText(execContext.messageContent, "消息内容不能为空");
        Assert.hasText(execContext.receiverId, "消息接收者不能为空");

        FeishuCredential credential = getFeishuCredential(context, execContext.credentialId);

        Client client = Client.newBuilder(credential.getAppId(), credential.getAppSecret())
                .logReqAtDebug(true)
                .build();

        String content = buildReplyMessageContent(execContext);

        CreateMessageReq req = CreateMessageReq.newBuilder()
                .receiveIdType(execContext.receiverIdType.getValue())
                .createMessageReqBody(CreateMessageReqBody.newBuilder()
                        .receiveId(execContext.receiverId)
                        .msgType(execContext.msgType.getValue())
                        .uuid(R.UU16())
                        .content(content)
                        .build())
                .build();

        CreateMessageResp resp = client.im().message().create(req);

        if (resp.getCode() != 0) {
            String errorMsg = String.format("飞书消息发送失败: code=%d, msg=%s", resp.getCode(), resp.getMsg());
            log.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }

        log.info("飞书消息发送成功: receiverId={}", execContext.receiverId);
        return JsonUtils.obj2Json(Map.of(
                "code", resp.getCode(),
                "msg", resp.getMsg() != null ? resp.getMsg() : "success",
                "requestId", resp.getRequestId() != null ? resp.getRequestId() : ""
        ));
    }

    /**
     * 回复飞书消息
     */
    private String replyFeishuMessage(FlowContext context, FeishuExecutionContext execContext) throws Exception {
        Assert.hasText(execContext.messageContent, "消息内容不能为空");
        Assert.hasText(execContext.messageId, "待回复的消息ID不能为空");

        FeishuCredential credential = getFeishuCredential(context, execContext.credentialId);

        Client client = Client.newBuilder(credential.getAppId(), credential.getAppSecret())
                .logReqAtDebug(true)
                .build();

        String content = buildReplyMessageContent(execContext);

        ReplyMessageReq req = ReplyMessageReq.newBuilder()
                .messageId(execContext.messageId)
                .replyMessageReqBody(ReplyMessageReqBody.newBuilder()
                        .content(content)
                        .msgType(execContext.msgType.getValue())
                        .replyInThread(execContext.replyInThread)
                        .uuid(R.UU16())
                        .build())
                .build();

        ReplyMessageResp resp = client.im().v1().message().reply(req);

        if (!resp.success()) {
            String errorMsg = String.format("飞书消息回复失败: code=%d, msg=%s", resp.getCode(), resp.getMsg());
            log.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }

        log.info("飞书消息回复成功: messageId={}", execContext.messageId);
        return JsonUtils.obj2Json(Map.of(
                "code", resp.getCode(),
                "msg", resp.getMsg() != null ? resp.getMsg() : "success",
                "requestId", resp.getRequestId() != null ? resp.getRequestId() : ""
        ));
    }

    /**
     * 构建发送消息内容
     */
    private String buildMessageContent(FeishuExecutionContext execContext) {
        if (execContext.msgType == FeishuNode.MsgType.TEXT) {
            Map<String, Object> contentMap = new HashMap<>();
            Map<String, Object> textMap = new HashMap<>();
            textMap.put("text", execContext.messageContent);

            List<Map<String, Object>> atList = new ArrayList<>();
            if (StringUtils.isNotBlank(execContext.at)) {
                String[] atUsers = execContext.at.split(",");
                for (String userId : atUsers) {
                    Map<String, Object> atItem = new HashMap<>();
                    atItem.put("tag", "user");
                    atItem.put("user_id", userId.trim());
                    atList.add(atItem);
                }
            }
            if (execContext.atAll) {
                Map<String, Object> atAllItem = new HashMap<>();
                atAllItem.put("tag", "all");
                atList.add(atAllItem);
            }

            textMap.put("at", atList);
            contentMap.put("zh_cn", textMap);
            return JsonUtils.obj2Json(contentMap);
        }
        return execContext.messageContent;
    }

    /**
     * 构建回复消息内容
     */
    private String buildReplyMessageContent(FeishuExecutionContext execContext) {
        if (execContext.msgType == FeishuNode.MsgType.TEXT) {
            Map<String, Object> textMap = new HashMap<>();
            textMap.put("text", execContext.messageContent);
            return JsonUtils.obj2Json(textMap);
        }
        return execContext.messageContent;
    }

    /**
     * 获取飞书凭据
     */
    private FeishuCredential getFeishuCredential(FlowContext context, String credentialId) {
        Assert.hasText(credentialId, "飞书应用凭据ID不能为空");
        return context.getEngineServices().getCredentialsManager().getFeishuCredential(credentialId);
    }

    /**
     * 构建结果映射
     */
    private Map<String, Object> buildResultMap(String result, FeishuExecutionContext execContext) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put(RESULT, result);
        resultMap.put("operationType", execContext.operationType.name());
        if (execContext.operationType == FeishuNode.OperationType.REPLY) {
            resultMap.put("messageId", execContext.messageId);
        } else {
            resultMap.put("receiverIdType", execContext.receiverIdType.name());
            resultMap.put("receiverId", execContext.receiverId);
        }
        resultMap.put("msgType", execContext.msgType.name());
        return resultMap;
    }

    /**
     * 飞书执行上下文
     */
    @lombok.Builder
    @lombok.Data
    private static class FeishuExecutionContext {
        private String credentialId;
        private FeishuNode.OperationType operationType;
        private FeishuNode.ReceiverIdType receiverIdType;
        private String receiverId;
        private String messageId;
        private FeishuNode.MsgType msgType;
        private String messageContent;
        private String at;
        private boolean atAll;
        private boolean replyInThread;
    }
}
