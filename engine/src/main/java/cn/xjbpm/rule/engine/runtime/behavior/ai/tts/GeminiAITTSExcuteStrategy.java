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
package cn.xjbpm.rule.engine.runtime.behavior.ai.tts;

import cn.hutool.core.collection.CollUtil;
import cn.xjbpm.rule.engine.aviator.AviatorContext;
import cn.xjbpm.rule.engine.aviator.AviatorExecutor;
import cn.xjbpm.rule.engine.definition.model.nodes.AITTSNode;
import cn.xjbpm.rule.engine.runtime.behavior.ai.AIExcuteStrategy;
import cn.xjbpm.rule.engine.runtime.model.FlowContext;
import cn.xjbpm.rule.engine.runtime.model.credentials.ApikeyCredential;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2026/1/30
 */
@Slf4j
public class GeminiAITTSExcuteStrategy implements AIExcuteStrategy<AITTSNode> {
    @Override
    public String getUrl(AITTSNode node) {
        return "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-preview-tts:generateContent";
    }

    @Override
    public Map<String, String> getHeaders(ApikeyCredential apikeyCredential) {
        Map<String, String> headers = new HashMap<>();
        headers.put("x-goog-api-key", apikeyCredential.getApiKey());
        headers.put("Content-Type", "application/json");
        return headers;
    }

    @Override
    public Map<String, Object> getRequestBody(AITTSNode node, FlowContext context) {
        String userPrompt = AviatorExecutor.evaluateString(AviatorContext.create(node.getPrompt(), context.getVariable()));
        Map<String, Object> part = new HashMap<>();
        part.put("text", userPrompt);

        Map<String, Object> contentItem = new HashMap<>();
        contentItem.put("parts", Collections.singletonList(part));

        Map<String, Object> speechConfig = new HashMap<>();

        if (node.getSpeaker() == AITTSNode.Speaker.singleSpeaker) {
            speechConfig.put("voiceConfig", Collections.singletonMap("prebuiltVoiceConfig", Collections.singletonMap("voiceName", node.getVoice())));
        }
        if (node.getSpeaker() == AITTSNode.Speaker.multiSpeaker && CollUtil.isNotEmpty(node.getMultiSpeakerVoiceConfig())) {
            List<Map> voiceConfigs = new ArrayList<>();
            for (AITTSNode.MultiSpeaker multiSpeaker : node.getMultiSpeakerVoiceConfig()) {
                Map<String, Object> speaker = new HashMap<>();
                speaker.put("speaker", multiSpeaker.getSpeaker());
                speaker.put("voiceConfig", Collections.singletonMap("prebuiltVoiceConfig", Collections.singletonMap("voiceName", multiSpeaker.getVoice())));
                voiceConfigs.add(speaker);
            }
            speechConfig.put("multiSpeakerVoiceConfig", Collections.singletonMap("speakerVoiceConfigs", voiceConfigs));
        }

        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("responseModalities", Collections.singletonList("AUDIO"));
        generationConfig.put("speechConfig", speechConfig);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(contentItem));
        requestBody.put("generationConfig", generationConfig);
        requestBody.put("model", "gemini-2.5-flash-preview-tts");
        return requestBody;
    }
}