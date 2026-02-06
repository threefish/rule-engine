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

package cn.xjbpm.rule.engine.runtime.behavior.ai.image;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.xjbpm.rule.engine.aviator.AviatorContext;
import cn.xjbpm.rule.engine.aviator.AviatorExecutor;
import cn.xjbpm.rule.engine.definition.model.nodes.AIImageNode;
import cn.xjbpm.rule.engine.runtime.behavior.ai.AIExcuteStrategy;
import cn.xjbpm.rule.engine.runtime.model.FlowContext;
import cn.xjbpm.rule.engine.runtime.model.credentials.ApikeyCredential;

import java.util.*;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2026/1/29
 */
public class GeminiAIImageExcuteStrategy implements AIExcuteStrategy<AIImageNode> {

    private static final String MODEL_IMAGEN_4_0_GENERATE_001 = "imagen-4.0-generate-001";

    @Override
    public String getUrl(AIImageNode node) {
        String model = node.getModel();
        if (MODEL_IMAGEN_4_0_GENERATE_001.equals(model)) {
            return "https://generativelanguage.googleapis.com/v1beta/models/imagen-4.0-generate-001:predict";
        }
        return "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent";
    }

    @Override
    public Map<String, String> getHeaders(ApikeyCredential apikeyCredential) {
        Map<String, String> headers = new HashMap<>();
        headers.put("x-goog-api-key", apikeyCredential.getApiKey());
        headers.put("Content-Type", "application/json");
        return headers;
    }


    @Override
    public Map<String, Object> getRequestBody(AIImageNode node, FlowContext context) {
        Map<String, Object> requestBody = new HashMap<>();
        String userPrompt = AviatorExecutor.evaluateString(AviatorContext.create(node.getPrompt(), context.getVariable()));
        if (MODEL_IMAGEN_4_0_GENERATE_001.equals(node.getModel())) {
            requestBody.put("instances", List.of(Collections.singletonMap("prompt", userPrompt)));
            return requestBody;
        }

        List<Map> parts = new ArrayList<>();
        parts.add(Collections.singletonMap("text", userPrompt));
        List<AIImageNode.Image> images = node.getImages();
        if (CollUtil.isNotEmpty(images)) {
            for (AIImageNode.Image image : images) {
                Map map = new HashMap();
                map.put("mime_type", "image/jpeg");
                map.put("data", image.getUrl());
                parts.add(Collections.singletonMap("inline_data", map));
            }
        }
        Map generationConfig = new HashMap();
        Map<String, Object> properties = node.getProperties();
        String responseModalities = String.valueOf(properties.getOrDefault("responseModalities", ""));
        generationConfig.put("responseModalities", Arrays.asList("IMAGE", "TEXT"));
        if (Objects.equals(responseModalities, "IMAGE")) {
            generationConfig.put("responseModalities", List.of("IMAGE"));
        }
        Map<String, Object> imageConfig = new HashMap<>();
        String aspectRatio = String.valueOf(properties.getOrDefault("aspectRatio", ""));
        String imageSize = String.valueOf(properties.getOrDefault("imageSize", ""));
        if (StrUtil.isNotBlank(aspectRatio)) {
            imageConfig.put("aspectRatio", aspectRatio);
        }
        if (StrUtil.isNotBlank(imageSize)) {
            imageConfig.put("imageSize", imageSize);
        }
        if (!imageConfig.isEmpty()) {
            generationConfig.put("imageConfig", imageConfig);
        }

        requestBody.put("contents", List.of(Collections.singletonMap("parts", parts)));
        requestBody.put("generationConfig", generationConfig);
        return requestBody;
    }
}