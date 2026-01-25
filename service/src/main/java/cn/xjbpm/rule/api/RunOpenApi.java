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
package cn.xjbpm.rule.api;

import cn.xjbpm.rule.common.utils.JsonUtils;
import cn.xjbpm.rule.dto.ExcuteRuleFlow;
import cn.xjbpm.rule.dto.ExcutingHistoryLogVO;
import cn.xjbpm.rule.engine.runtime.RuleFlowExcuteService;
import cn.xjbpm.rule.engine.runtime.model.ExecutStatus;
import cn.xjbpm.rule.error.AuthoriztionConstant;
import cn.xjbpm.rule.repository.entity.ExcuteLogEntity;
import cn.xjbpm.rule.service.ExcuteLogService;
import cn.xjbpm.rule.service.OpenApiAuthoriztionService;
import cn.xjbpm.rule.vo.common.ResultVO;
import cn.xjbpm.rule.vo.excute.ExcuteRuleFlowRequest;
import cn.xjbpm.rule.vo.excute.ExcuteRuleFlowResponse;
import cn.xjbpm.rule.vo.excute.QueryExcuteRuleFlowRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author 黄川 huchuc@vip.qq.com
 */
@RestController
@RequestMapping("/openapi/v1/ruleflow")
@RequiredArgsConstructor
@Slf4j
public class RunOpenApi {

    private static final String HEADER_NAME = "Authorization";
    private final RuleFlowExcuteService ruleFlowExcuteService;
    private final OpenApiAuthoriztionService openApiAuthoriztionService;
    private final ExcuteLogService excuteLogService;

    @PostMapping("/excute")
    public ResultVO<ExcuteRuleFlowResponse> excute(@Validated
                                                   @RequestBody ExcuteRuleFlowRequest request,
                                                   @RequestHeader(value = HEADER_NAME) String token) {
        if (!openApiAuthoriztionService.validate(token, request.getKey())) {
            return ResultVO.fail(AuthoriztionConstant.AUTHORIZTION_ERROR);
        }
        ExcuteRuleFlow excuteRuleFlowRequest = new ExcuteRuleFlow();
        if (Objects.nonNull(request.getRetryOriginId())) {
            ExcuteLogEntity entity = excuteLogService.findById(request.getRetryOriginId());
            if (Objects.isNull(entity)) {
                return ResultVO.fail("重试失败，当前 ID:%s 的规则流尚未执行完成或者不存在！");
            }
            ExcutingHistoryLogVO vo = JsonUtils.json2Obj(entity.getContent(), ExcutingHistoryLogVO.class);
            Set<String> nodes = vo.getNodeExcutions().entrySet().stream()
                    .filter(entry -> entry.getValue().getStatus() == ExecutStatus.SUCCESS)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet());
            excuteRuleFlowRequest.setSkipNodeIds(nodes);
            excuteRuleFlowRequest.setContent(vo.getRuleFlowOriginalJson());
        }
        excuteRuleFlowRequest.setKey(request.getKey());
        excuteRuleFlowRequest.setVariables(request.getVariables());
        excuteRuleFlowRequest.setRequestId(request.getRequestId());
        excuteRuleFlowRequest.setRetryOriginId(request.getRetryOriginId());
        excuteRuleFlowRequest.setAsyncExcute(request.isAsyncExcute());
        return ResultVO.success(ExcuteRuleFlowResponse.create(ruleFlowExcuteService.startFlow(excuteRuleFlowRequest)));
    }

    @PostMapping("/query")
    @SuppressWarnings("all")
    public ResultVO<ExcuteRuleFlowResponse> query(@Validated
                                                  @RequestBody QueryExcuteRuleFlowRequest request,
                                                  @RequestHeader(value = HEADER_NAME) String token) {

        ExcuteLogEntity entity;
        if (Objects.nonNull(request.getRequestId())) {
            entity = excuteLogService.findByRequestId(request.getRequestId());
        } else if (Objects.nonNull(request.getId())) {
            entity = excuteLogService.findById(request.getId());
        } else {
            return ResultVO.fail("requestId 和 id 不能同时为空！");
        }
        if (Objects.isNull(entity)) {
            return ResultVO.fail("记录不存在或者未执行完成，请稍后再试");
        }
        if (openApiAuthoriztionService.validate(token, entity.getRuleFlowKey()) == false) {
            return ResultVO.fail(AuthoriztionConstant.AUTHORIZTION_ERROR);
        }
        ExcutingHistoryLogVO vo = JsonUtils.json2Obj(entity.getContent(), ExcutingHistoryLogVO.class);
        ExcuteRuleFlowResponse excuteRuleFlowResponse = new ExcuteRuleFlowResponse();
        excuteRuleFlowResponse.setId(entity.getId());
        excuteRuleFlowResponse.setRequestId(entity.getRequestId());
        excuteRuleFlowResponse.setRuleFlowKey(entity.getRuleFlowKey());
        excuteRuleFlowResponse.setResponse(vo.getResponse());
        excuteRuleFlowResponse.setTimeConsuming(entity.getTimeConsuming());
        excuteRuleFlowResponse.setStatus(entity.getStatus());
        excuteRuleFlowResponse.setErrorMessage(entity.getErrorMessage());
        return ResultVO.success(excuteRuleFlowResponse);
    }

}