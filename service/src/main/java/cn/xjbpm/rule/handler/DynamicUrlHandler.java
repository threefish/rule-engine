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

package cn.xjbpm.rule.handler;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import cn.xjbpm.rule.common.utils.HttpHeaderUtils;
import cn.xjbpm.rule.common.utils.JsonUtils;
import cn.xjbpm.rule.dto.ExcuteRuleFlow;
import cn.xjbpm.rule.dto.ExcuteRuleFlowResult;
import cn.xjbpm.rule.engine.runtime.RuleFlowExcuteService;
import cn.xjbpm.rule.handler.auth.DynamicUrlAuthenticator;
import cn.xjbpm.rule.handler.auth.DynamicUrlAuthenticatorManager;
import cn.xjbpm.rule.repository.entity.DynamicUrlMappingEntity;
import cn.xjbpm.rule.repository.enums.AuthType;
import cn.xjbpm.rule.vo.common.ResultVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * 动态URL请求处理器
 */
@Slf4j
public class DynamicUrlHandler {

    private final RuleFlowExcuteService ruleFlowExcuteService;
    private final Map<String, DynamicUrlMappingEntity> mappingCache;
    private final DynamicUrlAuthenticatorManager authenticatorManager;

    public DynamicUrlHandler(RuleFlowExcuteService ruleFlowExcuteService,
                             Map<String, DynamicUrlMappingEntity> mappingCache,
                             DynamicUrlAuthenticatorManager authenticatorManager) {
        this.ruleFlowExcuteService = ruleFlowExcuteService;
        this.mappingCache = mappingCache;
        this.authenticatorManager = authenticatorManager;
    }

    @ResponseBody
    public Object handle(HttpServletRequest request) {
        String httpMethod = request.getMethod();
        String requestURI = request.getRequestURI();
        String mappingKey = buildMappingKey(requestURI, httpMethod);
        DynamicUrlMappingEntity mapping = mappingCache.get(mappingKey);
        if (mapping == null) {
            log.warn("未找到动态URL映射: {} {}", httpMethod, requestURI);
            return ResultVO.fail("未找到URL映射");
        }
        log.info("动态URL请求: {} {}", mapping.getHttpMethod(), mapping.getUrlPath());
        
        DynamicUrlAuthenticator.AuthResult authResult = authenticate(request, mapping);
        if (!authResult.isSuccess()) {
            log.warn("动态URL认证失败: {} {}, 原因: {}", mapping.getHttpMethod(), mapping.getUrlPath(), authResult.getErrorMessage());
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ResultVO.fail(authResult.getErrorMessage()));
        }
        
        try {
            return handleRuleFlow(request, mapping);
        } catch (Exception e) {
            log.error("动态URL处理异常: {} {}", mapping.getHttpMethod(), mapping.getUrlPath(), e);
            return ResultVO.fail("处理请求失败: " + e.getMessage());
        }
    }

    /**
     * 执行认证
     */
    private DynamicUrlAuthenticator.AuthResult authenticate(HttpServletRequest request, DynamicUrlMappingEntity mapping) {
        AuthType authType = mapping.getAuthType();
        if (authType == null || authType == AuthType.NONE) {
            return DynamicUrlAuthenticator.AuthResult.success();
        }
        
        DynamicUrlAuthenticator authenticator = authenticatorManager.getAuthenticator(authType);
        if (authenticator == null) {
            log.warn("未找到认证处理器: {}", authType);
            return DynamicUrlAuthenticator.AuthResult.fail("认证处理器未配置");
        }
        
        return authenticator.authenticate(request, mapping);
    }

    private Object handleRuleFlow(HttpServletRequest request, DynamicUrlMappingEntity mapping) {
        Map<String, Object> variables = extractVariables(request);
        ExcuteRuleFlow excuteRuleFlow = new ExcuteRuleFlow();
        excuteRuleFlow.setKey(mapping.getRuleFlowKey());
        excuteRuleFlow.setVariables(variables);
        excuteRuleFlow.setRequestId(IdUtil.fastSimpleUUID());
        excuteRuleFlow.setAsyncExcute(mapping.getAsync());
        log.info("开始执行规则流程: {} 规则数据：{}", mapping.getRuleFlowKey(), JsonUtils.obj2Json(excuteRuleFlow));
        ExcuteRuleFlowResult result = ruleFlowExcuteService.startFlow(excuteRuleFlow);
        return result.getResponse();
    }


    public Map<String, Object> extractVariables(HttpServletRequest request) {
        Map<String, Object> variables = new HashMap<>();
        Map<String, Object> headers = new HashMap<>();
        // 1. 提取 Headers (生产环境建议过滤掉冗余的长 Header)
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            // 建议增加前缀区分，防止与参数冲突，或只提取特定业务 Header
            headers.put(name.toLowerCase(), request.getHeader(name));
        }
        variables.put("headers", HttpHeaderUtils.toCamelCaseMap(headers));
        // 2. 提取常规参数 (Query String 和 x-www-form-urlencoded)
        Map<String, Object> parameters = new HashMap<>();
        Map<String, String[]> parameterMap = request.getParameterMap();
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            String[] values = entry.getValue();
            if (values != null) {
                // 如果只有一个值则存 String，多个值存 List
                parameters.put(entry.getKey(), values.length == 1 ? values[0] : Arrays.asList(values));
            }
        }
        variables.put("parameters", parameters);
        // 3. 处理 JSON 请求体 (针对 POST/PUT 且 Content-Type 为 application/json)
        String contentType = request.getContentType();
        String method = request.getMethod().toUpperCase();
        boolean contains = Arrays.asList(HttpMethod.POST.name(), HttpMethod.PUT.name(), HttpMethod.PATCH.name()).contains(method);
        if (contentType != null && contentType.contains("application/json") && contains) {
            try {
                String jsonContent = IoUtil.read(request.getReader());
                variables.put("body", jsonContent);
                variables.put("jsonBody", JsonUtils.json2Obj(jsonContent, Object.class));
            } catch (IOException e) {
                log.error("处理JSON请求体异常", e);
                // 忽略
            }
        }
        return variables;
    }

    private String buildMappingKey(String urlPath, String httpMethod) {
        return httpMethod.toUpperCase() + ":" + urlPath;
    }

}
