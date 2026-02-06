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

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.xjbpm.rule.common.utils.FilePathValidatorUtils;
import cn.xjbpm.rule.common.utils.JsonUtils;
import cn.xjbpm.rule.common.utils.StringUtils;
import cn.xjbpm.rule.common.utils.http.HttpCallResult;
import cn.xjbpm.rule.common.utils.http.HttpCallUtil;
import cn.xjbpm.rule.common.utils.http.TimeoutOptions;
import cn.xjbpm.rule.custom.CredentialsManager;
import cn.xjbpm.rule.engine.aviator.AviatorContext;
import cn.xjbpm.rule.engine.aviator.AviatorExecutor;
import cn.xjbpm.rule.engine.definition.model.nodes.DownloadFileNode;
import cn.xjbpm.rule.engine.runtime.behavior.model.FileModel;
import cn.xjbpm.rule.engine.runtime.model.FlowContext;
import cn.xjbpm.rule.engine.runtime.model.credentials.HttpCredential;
import cn.xjbpm.rule.properties.RuleProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/30
 */
@Slf4j
@SuppressWarnings("all")
public class DownloadFileNodeBehavior implements NodeBehavior {

    private final DownloadFileNode node;

    public DownloadFileNodeBehavior(DownloadFileNode node) {
        this.node = node;
    }

    @Override
    public void execution(FlowContext context) throws Exception {
        log.info("Starting Excute HttpNode");
        RuleProperties ruleProperties = context.getBeanContextManager().getRuleProperties();
        Map<String, String> headers = new HashMap<>();
        if (CollUtil.isNotEmpty(node.getHeaders())) {
            for (DownloadFileNode.Header header : node.getHeaders()) {
                if (header.isEnabled() && StringUtils.isNotBlank(header.getKey()) && StringUtils.isNotBlank(header.getValue())) {
                    headers.put(header.getKey(), AviatorExecutor.evaluateString(AviatorContext.create(header.getValue(), context.getVariable())));
                }
            }
        }
        Map<String, String> queryParams = new HashMap<>();
        if (CollUtil.isNotEmpty(node.getQueryParams())) {
            for (DownloadFileNode.QueryParam queryParam : node.getQueryParams()) {
                if (queryParam.isEnabled() && StringUtils.isNotBlank(queryParam.getKey()) && StringUtils.isNotBlank(queryParam.getValue())) {
                    queryParams.put(queryParam.getKey(), AviatorExecutor.evaluateString(AviatorContext.create(queryParam.getValue(), context.getVariable())));
                }
            }
        }
        CredentialsManager credentialsManager = context.getBeanContextManager().getCredentialsManager();
        HttpCredential httpCredentials = credentialsManager.getHttpCredential(node.getCredentialId());
        if (httpCredentials.getQuerys() != null) {
            queryParams.putAll(httpCredentials.getQuerys());
        }
        if (httpCredentials.getHeaders() != null) {
            headers.putAll(httpCredentials.getHeaders());
        }
        String body = node.getBody();
        if (node.getEnableBody() == true) {
            body = AviatorExecutor.evaluateString(AviatorContext.create(node.getBody(), context.getVariable()));
        }
        String localSaveFilePath = AviatorExecutor.evaluateString(AviatorContext.create(node.getLocalSaveFilePath(), context.getVariable()));
        String url = AviatorExecutor.evaluateString(AviatorContext.create(node.getUrl(), context.getVariable()));
        Assert.hasText(localSaveFilePath, "本地存储文件路径不能为空");
        String ruleFlowKey = context.getProcessInstance().getRuleFlowKey();
        Path filePath = Paths.get(ruleProperties.getAttachmentPath(), ruleFlowKey, node.getId(), localSaveFilePath);
        FilePathValidatorUtils.validateForWrite(localSaveFilePath);
        File file = filePath.toFile();
        TimeoutOptions timeout = TimeoutOptions.VERAY_SLOW.copy();
        if (StringUtils.isNotBlank(node.getProxy())) {
            timeout.setProxyHost(node.getProxy());
        }
        HttpCallResult httpCallResult = HttpCallUtil.executeStreaming(node.getMethod(), url, body, headers, queryParams, timeout, (inputStream, clientHttpResponse) -> {
            FileUtil.writeFromStream(inputStream, file);
        });
        if (context.isDebugModel()) {
            context.addTraceLog(node.getId(), "http call params:'{}' header:'{}' body:'{}' result:'{}'", new Object[]{JsonUtils.obj2Json(queryParams), JsonUtils.obj2Json(headers), body, JsonUtils.obj2Json(httpCallResult)});
        }
        if (log.isInfoEnabled()) {
            log.info("DownloadFileNode Http Call Result:{}", JsonUtils.obj2Json(httpCallResult));
        }
        Map<String, Object> response = httpCallResult.toMap();
        response.put("file", FileModel.of(file));
        context.put(node.getId(), response);
        if (httpCallResult.is2xx() == false) {
            throw new RuntimeException(String.format("调用失败！状态码:%s 请求地址:%s", httpCallResult.getStatusCode(), url));
        }
    }

}