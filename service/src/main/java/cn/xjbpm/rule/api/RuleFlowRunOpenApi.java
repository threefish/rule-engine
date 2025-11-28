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
import cn.hutool.core.util.StrUtil;
import cn.xjbpm.rule.dto.ExcuteRuleFlowVO;
import cn.xjbpm.rule.engine.runtime.ProcessRunService;
import cn.xjbpm.rule.vo.ResultVO;
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
@RequestMapping("/open/ruleflow")
@RequiredArgsConstructor
@Slf4j
public class RuleFlowRunOpenApi {

    private final ProcessRunService processRunService;

    @PostMapping("/excute")
    public ResultVO<ExcuteRuleFlowVO.Response> excute(@Validated @RequestBody ExcuteRuleFlowVO.Request request) {
        try {
            if (StrUtil.isBlank(request.getRequestId())) {
                request.setRequestId(IdUtil.getSnowflakeNextIdStr());
            }
            ExcuteRuleFlowVO.Response result = processRunService.testExcute(request);
            return ResultVO.success(result);
        } catch (Exception e) {
            log.error("执行出错：{}", e.getMessage(), e);
            return ResultVO.fail(e.getMessage());
        }
    }
}