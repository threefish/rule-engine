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
import cn.xjbpm.rule.common.utils.JsonUtils;
import cn.xjbpm.rule.common.utils.StringUtils;
import cn.xjbpm.rule.common.utils.http.HttpCallResult;
import cn.xjbpm.rule.common.utils.http.HttpCallUtil;
import cn.xjbpm.rule.common.utils.http.TimeoutOptions;
import cn.xjbpm.rule.custom.CredentialsManager;
import cn.xjbpm.rule.engine.definition.model.nodes.AITTSNode;
import cn.xjbpm.rule.engine.runtime.behavior.ai.AIExcuteStrategy;
import cn.xjbpm.rule.engine.runtime.behavior.ai.tts.GeminiAITTSExcuteStrategy;
import cn.xjbpm.rule.engine.runtime.behavior.model.FileModel;
import cn.xjbpm.rule.engine.runtime.model.FlowContext;
import cn.xjbpm.rule.engine.runtime.model.credentials.ApikeyCredential;
import cn.xjbpm.rule.properties.RuleProperties;
import com.jayway.jsonpath.DocumentContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 */
@Slf4j
@SuppressWarnings("all")
public class AITTSNodeBehavior implements NodeBehavior {

    private static final Map<String, AIExcuteStrategy> services = new HashMap<>() {
        {
            put("gemini", new GeminiAITTSExcuteStrategy());
        }
    };
    private final AITTSNode node;

    public AITTSNodeBehavior(AITTSNode node) {
        this.node = node;
    }

    @Override
    public void execution(FlowContext context) throws Exception {
        RuleProperties ruleProperties = context.getBeanContextManager().getRuleProperties();

        CredentialsManager credentialsManager = context.getBeanContextManager().getCredentialsManager();
        ApikeyCredential apikeyCredential = credentialsManager.getApikeyCredential(node.getCredentialId());

        AIExcuteStrategy strategy = services.get(apikeyCredential.getType());
        String url = strategy.getUrl(node);
        Map<String, Object> body = strategy.getRequestBody(node, context);

        Map<String, String> headers = strategy.getHeaders(apikeyCredential);

        TimeoutOptions timeout = TimeoutOptions.VERAY_SLOW.copy();
        if (StringUtils.isNotBlank(node.getProxy())) {
            timeout.setProxyHost(node.getProxy());
        }
        HttpCallResult httpCallResult = HttpCallUtil.execute(HttpMethod.POST, url, body, headers, null, timeout);
        if (context.isDebugModel()) {
            context.addTraceLog(node.getId(), "http call result:{}", new Object[]{JsonUtils.obj2Json(httpCallResult)});
        }
        Map<String, Object> resultMap = httpCallResult.toMap();
        DocumentContext parse = JsonPathUtil.parse(httpCallResult.getBody());
        String base64Audio = parse.read("$.candidates[0].content.parts[0].inlineData.data", String.class);
        if (StringUtils.isNotBlank(base64Audio)) {
            byte[] pcmData = Base64.getDecoder().decode(base64Audio);
            String ruleFlowKey = context.getProcessInstance().getRuleFlowKey();
            Path filePath = Paths.get(ruleProperties.getAttachmentPath(), ruleFlowKey, node.getId(), System.currentTimeMillis() + ".wav");
            // 文件路径不存在时，先创建文件夹
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, convertPcmToWav(pcmData));
            resultMap.put("file", FileModel.of(filePath.toFile()));
        }
        if (log.isInfoEnabled()) {
            log.info("AITTSNodeBehavior Http Call Result:{}", JsonUtils.obj2Json(httpCallResult));
        }
        context.put(node.getId(), resultMap);
        httpCallResult.assertSuccess();
    }

    /**
     * "mimeType": "audio/L16;codec=pcm;rate=24000",
     *
     * @param pcmData
     * @return
     * @throws IOException
     */
    private byte[] convertPcmToWav(byte[] pcmData) throws IOException {
        int sampleRate = 24000;
        int channels = 1;
        int bitsPerSample = 16;
        int byteRate = sampleRate * channels * bitsPerSample / 8;
        int blockAlign = channels * bitsPerSample / 8;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeBytes("RIFF");
        dos.writeInt(Integer.reverseBytes(36 + pcmData.length));
        dos.writeBytes("WAVE");
        dos.writeBytes("fmt ");
        dos.writeInt(Integer.reverseBytes(16));
        dos.writeShort(Short.reverseBytes((short) 1));
        dos.writeShort(Short.reverseBytes((short) channels));
        dos.writeInt(Integer.reverseBytes(sampleRate));
        dos.writeInt(Integer.reverseBytes(byteRate));
        dos.writeShort(Short.reverseBytes((short) blockAlign));
        dos.writeShort(Short.reverseBytes((short) bitsPerSample));
        dos.writeBytes("data");
        dos.writeInt(Integer.reverseBytes(pcmData.length));
        dos.write(pcmData);

        dos.close();
        return baos.toByteArray();
    }
}