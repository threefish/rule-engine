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
import cn.xjbpm.rule.engine.aviator.AviatorContext;
import cn.xjbpm.rule.engine.aviator.AviatorExecutor;
import cn.xjbpm.rule.engine.definition.model.nodes.AIImageNode;
import cn.xjbpm.rule.engine.runtime.behavior.ai.AIExcuteStrategy;
import cn.xjbpm.rule.engine.runtime.model.FlowContext;
import cn.xjbpm.rule.engine.runtime.model.credentials.ApikeyCredential;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2026/1/29
 */
@SuppressWarnings("all")
public class VolcengineAIImageExcuteStrategy implements AIExcuteStrategy<AIImageNode> {
    @Override
    public String getUrl(AIImageNode node) {
        return "https://ark.cn-beijing.volces.com/api/v3/images/generations";
    }

    @Override
    public Map<String, String> getHeaders(ApikeyCredential apikeyCredential) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + apikeyCredential.getApiKey());
        headers.put("Content-Type", "application/json");
        return headers;
    }

    @Override
    public Map<String, Object> getRequestBody(AIImageNode node, FlowContext context) {
        String userPrompt = AviatorExecutor.evaluateString(
                AviatorContext.create(node.getPrompt(), context.getVariable())
        );

        Map<String, Object> properties = node.getProperties();
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", node.getModel());
        requestBody.put("prompt", userPrompt);
        requestBody.put("stream", false);
        requestBody.put("watermark", false);
        requestBody.put("response_format", String.valueOf(properties.getOrDefault("responseFormat", "url")));
        requestBody.put("size", String.valueOf(properties.getOrDefault("size", "")));

        String sequentialImageGeneration = String.valueOf(properties.getOrDefault("sequentialImageGeneration", ""));
        requestBody.put("sequential_image_generation", sequentialImageGeneration);
        if ("auto".equals(sequentialImageGeneration)) {
            Map<String, Object> options = new HashMap<>();
            String sequentialImageGenerationMaxImages = String.valueOf(properties.getOrDefault("sequentialImageGenerationMaxImages", "1"));
            options.put("max_images", Math.max(Long.parseLong(sequentialImageGenerationMaxImages), 1L));
            requestBody.put("sequential_image_generation_options", options);
        }
        if (CollUtil.isNotEmpty(node.getImages())) {
            if (node.getImages().size() == 1) {
                requestBody.put("image", node.getImages().get(0).getUrl());
            } else {
                List<String> stringList = new ArrayList<>();
                for (AIImageNode.Image image : node.getImages()) {
                    stringList.add(image.getUrl());
                }
                requestBody.put("image", stringList);
            }
        }
        return requestBody;
    }
}