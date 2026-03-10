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
import cn.hutool.core.util.RandomUtil;
import cn.xjbpm.rule.common.utils.JsonPathUtil;
import cn.xjbpm.rule.common.utils.JsonUtils;
import cn.xjbpm.rule.common.utils.StringUtils;
import cn.xjbpm.rule.common.utils.http.HttpCallResult;
import cn.xjbpm.rule.common.utils.http.HttpCallUtil;
import cn.xjbpm.rule.common.utils.http.TimeoutOptions;
import cn.xjbpm.rule.custom.CredentialsManager;
import cn.xjbpm.rule.engine.aviator.AviatorContext;
import cn.xjbpm.rule.engine.aviator.AviatorExecutor;
import cn.xjbpm.rule.engine.definition.model.nodes.HttpNode;
import cn.xjbpm.rule.engine.rule.Rule;
import cn.xjbpm.rule.engine.rule.enums.AssignmentType;
import cn.xjbpm.rule.engine.rule.enums.CombinatorType;
import cn.xjbpm.rule.engine.rule.enums.VariableType;
import cn.xjbpm.rule.engine.rule.translate.RuleExpressionTranslate;
import cn.xjbpm.rule.engine.runtime.model.FlowContext;
import cn.xjbpm.rule.engine.runtime.model.credentials.HttpCredential;
import com.jayway.jsonpath.DocumentContext;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/30
 */
@Slf4j
public class HttpNodeBehavior implements NodeBehavior {

    private final static String TEMP_VAR = "temp_value";
    private final HttpNode node;

    public HttpNodeBehavior(HttpNode node) {
        this.node = node;
    }

    @Override
    public void execution(FlowContext context) throws Exception {
        log.info("Starting Excute HttpNode");
        Map<String, String> headers = new HashMap<>();
        if (CollUtil.isNotEmpty(node.getHeaders())) {
            for (HttpNode.Header header : node.getHeaders()) {
                if (header.isEnabled() && StringUtils.isNotBlank(header.getKey()) && StringUtils.isNotBlank(header.getValue())) {
                    headers.put(header.getKey(), AviatorExecutor.evaluateString(AviatorContext.create(header.getValue(), context.getVariable())));
                }
            }
        }
        Map<String, String> queryParams = new HashMap<>();
        if (CollUtil.isNotEmpty(node.getQueryParams())) {
            for (HttpNode.QueryParam queryParam : node.getQueryParams()) {
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
        if (node.getEnableBody()) {
            body = AviatorExecutor.evaluateString(AviatorContext.create(node.getBody(), context.getVariable()));
        }
        TimeoutOptions timeout = getTimeout();
        if (StringUtils.isNotBlank(node.getProxy())) {
            timeout.setProxyHost(node.getProxy());
        }
        HttpCallResult httpCallResult = HttpCallUtil.execute(node.getMethod(), node.getUrl(), body, headers, queryParams, timeout);
        if (context.isDebugModel()) {
            context.addTraceLog(node.getId(), "http call params:'{}' header:'{}' body:'{}' result:'{}'", JsonUtils.obj2Json(queryParams), JsonUtils.obj2Json(headers), body, JsonUtils.obj2Json(httpCallResult));
        }
        if (log.isInfoEnabled()) {
            log.info("HttpNode Http Call Result:{}", JsonUtils.obj2Json(httpCallResult));
        }
        Map<String, Object> response = httpCallResult.toMap();
        context.put(node.getId(), response);
        if (httpCallResult.is2xx()) {
            HttpNode.ResponseCondition responseCondition = node.getResponseCondition();
            if (responseCondition != null && responseCondition.getConditions() != null) {
                DocumentContext parse = JsonPathUtil.parse(JsonUtils.obj2Json(response));
                boolean success = checkResponseData(parse);
                if (!success) {
                    throw new RuntimeException(String.format("调用成功 响应成功判断结果为否！请求地址:%s 响应内容:%s", node.getUrl(), httpCallResult.getBody()));
                }
            }
        } else {
            throw new RuntimeException(String.format("调用失败！状态码:%s 请求地址:%s", httpCallResult.getStatusCode(), node.getUrl()));
        }
    }

    private TimeoutOptions getTimeout() {
        if (node.getTimeout() == HttpNode.Timeout.FAST) {
            return TimeoutOptions.FAST;
        }
        if (node.getTimeout() == HttpNode.Timeout.SLOW) {
            return TimeoutOptions.SLOW;
        }
        if (node.getTimeout() == HttpNode.Timeout.VERAY_SLOW) {
            return TimeoutOptions.VERAY_SLOW;
        }
        return TimeoutOptions.DEFAULT;
    }


    private boolean checkResponseData(DocumentContext parse) {
        HttpNode.ResponseCondition responseCondition = node.getResponseCondition();
        if (responseCondition != null && responseCondition.getConditions() != null) {
            Rule rootRule = new Rule();
            rootRule.setRoot(true);
            rootRule.setCombinator(responseCondition.getCombination());
            rootRule.setRules(new ArrayList<>());
            Map<String, Object> variable = new HashMap<>();
            for (HttpNode.Condition condition : responseCondition.getConditions()) {
                String variableName = TEMP_VAR + RandomUtil.randomString(5);
                Object value = parse.read(condition.getName());
                variable.put(variableName, value);
                Rule rule = new Rule();
                rule.setOperator(condition.getOp());
                rule.setValue(condition.getValue());
                rule.setName(variableName);
                rule.setCombinator(CombinatorType.AND);
                rule.setAssignmentType(AssignmentType.FIXED);
                if (value instanceof Number) {
                    rule.setVarType(VariableType.NUMBER);
                }
                if (value instanceof Boolean) {
                    rule.setVarType(VariableType.BOOLEAN);
                }
                if (value instanceof Collection) {
                    rule.setVarType(VariableType.LIST);
                }
                if (value instanceof String || rule.getVarType() == null) {
                    rule.setVarType(VariableType.STRING);
                }
                rootRule.getRules().add(rule);
            }
            String expression = new RuleExpressionTranslate(rootRule).getExpression();
            return AviatorExecutor.executeBoolean(AviatorContext.create(expression, variable));
        }
        return true;
    }
}