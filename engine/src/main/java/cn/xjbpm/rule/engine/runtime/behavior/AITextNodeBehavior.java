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

import cn.xjbpm.rule.common.utils.JsonUtils;
import cn.xjbpm.rule.common.utils.StringUtils;
import cn.xjbpm.rule.common.utils.http.HttpCallResult;
import cn.xjbpm.rule.common.utils.http.HttpCallUtil;
import cn.xjbpm.rule.common.utils.http.TimeoutOptions;
import cn.xjbpm.rule.custom.CredentialsManager;
import cn.xjbpm.rule.engine.definition.model.nodes.AITextNode;
import cn.xjbpm.rule.engine.runtime.behavior.ai.AIExcuteStrategy;
import cn.xjbpm.rule.engine.runtime.behavior.ai.text.DeepseekAITextExcuteStrategy;
import cn.xjbpm.rule.engine.runtime.behavior.ai.text.VolcengineAITextExcuteStrategy;
import cn.xjbpm.rule.engine.runtime.model.FlowContext;
import cn.xjbpm.rule.engine.runtime.model.credentials.ApikeyCredential;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/30
 */
@SuppressWarnings("all")
@Slf4j
public class AITextNodeBehavior implements NodeBehavior {

    private static final Map<String, AIExcuteStrategy> services = new HashMap<>() {
        {
            put("volcengine", new VolcengineAITextExcuteStrategy());
            put("deepseek", new DeepseekAITextExcuteStrategy());
        }
    };

    private final AITextNode node;


    public AITextNodeBehavior(AITextNode node) {
        this.node = node;
    }

    @Override
    public void execution(FlowContext context) throws Exception {
        CredentialsManager credentialsManager = context.getEngineServices().getCredentialsManager();
        ApikeyCredential apikeyCredential = credentialsManager.getApikeyCredential(node.getCredentialId());
        String type = apikeyCredential.getType();
        AIExcuteStrategy strategy = services.get(type);
        String url = strategy.getUrl(node);

        Map<String, String> headers = strategy.getHeaders(apikeyCredential);

        Map<String, Object> requestBody = strategy.getRequestBody(node, context);

        TimeoutOptions timeout = TimeoutOptions.VERAY_SLOW.copy();
        if (StringUtils.isNotBlank(node.getProxy())) {
            timeout.setProxyHost(node.getProxy());
        }
        HttpCallResult httpCallResult = HttpCallUtil.execute(HttpMethod.POST, url, requestBody, headers, null, timeout);
        if (context.isDebugModel()) {
            context.addTraceLog(node.getId(), "http call result:{}", new Object[]{JsonUtils.obj2Json(httpCallResult)});
        }
        if (log.isInfoEnabled()) {
            log.info("AITextNodeBehavior Http Call Result:{}", JsonUtils.obj2Json(httpCallResult));
        }
        context.setNodeOutput(node.getId(), httpCallResult.toMap());
        httpCallResult.assertSuccess();
    }

}