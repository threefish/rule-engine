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


package cn.xjbpm.rule.engine.runtime.behavior.ai.text;

import cn.hutool.core.collection.CollUtil;
import cn.xjbpm.rule.engine.aviator.AviatorContext;
import cn.xjbpm.rule.engine.aviator.AviatorExecutor;
import cn.xjbpm.rule.engine.definition.model.nodes.AITextNode;
import cn.xjbpm.rule.engine.runtime.behavior.ai.AIExcuteStrategy;
import cn.xjbpm.rule.engine.runtime.model.FlowContext;
import cn.xjbpm.rule.engine.runtime.model.credentials.ApikeyCredential;

import java.util.*;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2026/1/27
 */
@SuppressWarnings("all")
public class VolcengineAITextExcuteStrategy implements AIExcuteStrategy<AITextNode> {


    @Override
    public String getUrl(AITextNode node) {
        return "https://ark.cn-beijing.volces.com/api/v3/chat/completions";
    }


    @Override
    public Map<String, String> getHeaders(ApikeyCredential apikeyCredential) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + apikeyCredential.getApiKey());
        headers.put("Content-Type", "application/json");
        return headers;
    }

    @Override
    public Map<String, Object> getRequestBody(AITextNode node, FlowContext context) {
        String userPrompt = AviatorExecutor.evaluateString(
                AviatorContext.create(node.getPrompt(), context.getVariable())
        );

        List<AITextNode.Message> messages = new ArrayList<>();
        messages.add(AITextNode.Message.ofUser(userPrompt));
        if (node.isRequireSpecificOutFormat()) {
            messages.add(AITextNode.Message.ofSystem(String.format("Please output the minified JSON response result in strict accordance with the following JSON structure：\n%s\n", node.getJsonExample())));
        }
        if (CollUtil.isNotEmpty(node.getMessages())) {
            for (AITextNode.Message message : node.getMessages()) {
                messages.add(message);
            }
        }

        Map<String, Object> body = new HashMap<>();
        body.put("model", node.getModel());
        body.put("stream", false);
        body.put("messages", messages);
        AITextNode.Thinking thinking = node.getThinking();
        if (Objects.nonNull(thinking) && thinking != AITextNode.Thinking.auto) {
            body.put("thinking", Collections.singletonMap("type", thinking));
        }
        return body;
    }
}