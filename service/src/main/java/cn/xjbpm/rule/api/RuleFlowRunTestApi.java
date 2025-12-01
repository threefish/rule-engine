
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

import cn.hutool.core.util.IdUtil;
import cn.xjbpm.rule.custom.RuleFlowDefinitionService;
import cn.xjbpm.rule.dto.ExcuteRuleFlowVO;
import cn.xjbpm.rule.engine.runtime.RuleFlowExcuteService;
import cn.xjbpm.rule.vo.ResultVO;
import cn.xjbpm.rule.vo.RuleFlowTestExcuteVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 黄川 huchuc@vip.qq.com
 */
@RestController
@RequestMapping("/manage/ruleflow/test")
@RequiredArgsConstructor
@Slf4j
public class RuleFlowRunTestApi {

    private final RuleFlowExcuteService processRunService;

    /**
     * 测试执行
     *
     * @param request
     * @return
     */
    @PostMapping("/excute")
    public ResultVO<ExcuteRuleFlowVO.Response> testExcute(@Validated @RequestBody RuleFlowTestExcuteVO request) {
        try {
            ExcuteRuleFlowVO.Request createProcessRequest = new ExcuteRuleFlowVO.Request();
            createProcessRequest.setRequestId(IdUtil.getSnowflakeNextIdStr());
            createProcessRequest.setVariables(request.getVariables());
            createProcessRequest.setKey(request.getKey());
            createProcessRequest.setContent(request.getContent());
            createProcessRequest.setSkipNodeIds(request.getSkipNodeIds());
            createProcessRequest.setAsyncExcute(false);
            return ResultVO.success(processRunService.startFlow(createProcessRequest));
        } catch (Exception e) {
            log.error("执行出错：{}", e.getMessage(), e);
            return ResultVO.fail(e.getMessage());
        }
    }
}