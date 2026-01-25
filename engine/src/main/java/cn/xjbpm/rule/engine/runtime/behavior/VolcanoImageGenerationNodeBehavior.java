/*
 * Copyright 2025 threefish.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.xjbpm.rule.engine.runtime.behavior;

import cn.hutool.core.collection.CollUtil;
import cn.xjbpm.rule.common.utils.JsonUtils;
import cn.xjbpm.rule.common.utils.StringUtils;
import cn.xjbpm.rule.common.utils.http.HttpCallResult;
import cn.xjbpm.rule.common.utils.http.HttpCallUtil;
import cn.xjbpm.rule.common.utils.http.TimeoutOptions;
import cn.xjbpm.rule.custom.CredentialsManager;
import cn.xjbpm.rule.engine.definition.model.nodes.VolcanoImageGenerationNode;
import cn.xjbpm.rule.engine.runtime.model.FlowContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 */
@Slf4j
public class VolcanoImageGenerationNodeBehavior implements NodeBehavior {

    private final VolcanoImageGenerationNode node;

    private String url = "https://ark.cn-beijing.volces.com/api/v3/images/generations";

    public VolcanoImageGenerationNodeBehavior(VolcanoImageGenerationNode node) {
        this.node = node;
    }

    @Override
    public void execution(FlowContext context) throws Exception {
        CredentialsManager credentialsManager = context.getBeanContextManager().getCredentialsManager();
        String apiKey = credentialsManager.getApiKeyCredential(node.getCredentialId());

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + apiKey);
        headers.put("Content-Type", "application/json");

        Map<String, Object> body = new HashMap<>();
        body.put("model", node.getModelId());
        body.put("prompt", node.getPrompt());
        body.put("size", node.getSize());
        body.put("response_format", node.getResponseFormat().name());
        body.put("stream", node.isStream());
        body.put("watermark", node.isWatermark());
        body.put("sequential_image_generation", node.getSequentialImageGeneration().name());
        if (node.getSequentialImageGeneration() == VolcanoImageGenerationNode.SequentialImageGeneration.auto) {
            Map<String, Object> options = new HashMap<>();
            options.put("max_images", Math.max(node.getSequentialImageGenerationMaxImages(), 1L));
            body.put("sequential_image_generation_options", options);
        }
        if (CollUtil.isNotEmpty(node.getImages())) {
            if (node.getImages().size() == 1) {
                body.put("image", node.getImages().get(0).getUrl());
            } else {
                List<String> stringList = new ArrayList<>();
                for (VolcanoImageGenerationNode.Image image : node.getImages()) {
                    stringList.add(image.getUrl());
                }
                body.put("image", stringList);
            }
        }
        HttpCallResult httpCallResult = HttpCallUtil.execute(HttpMethod.POST, url, body, headers, null, TimeoutOptions.VERAY_SLOW);
        if (context.isDebugModel()) {
            context.addTraceLog(StringUtils.format("[{}] http call result:{}", node.getId(), JsonUtils.obj2Json(httpCallResult)));
        }
        if (log.isInfoEnabled()) {
            log.info("VolcanoImageGeneration Http Call Result:{}", JsonUtils.obj2Json(httpCallResult));
        }
        context.put(node.getId(), httpCallResult.toMap());
        if (!httpCallResult.is2xx()) {
            throw new RuntimeException(String.format("调用失败！状态码:%s", httpCallResult.getStatusCode()));
        }
    }
}