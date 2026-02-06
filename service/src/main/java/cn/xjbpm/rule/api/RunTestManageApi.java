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


package cn.xjbpm.rule.api;

import cn.hutool.core.util.IdUtil;
import cn.xjbpm.rule.dto.ExcuteRuleFlow;
import cn.xjbpm.rule.dto.ExcuteRuleFlowResult;
import cn.xjbpm.rule.engine.runtime.RuleFlowExcuteService;
import cn.xjbpm.rule.manager.DebugRealTimeDataManager;
import cn.xjbpm.rule.vo.common.IDRequestVO;
import cn.xjbpm.rule.vo.common.ResultVO;
import cn.xjbpm.rule.vo.excute.RuleFlowTestExcuteVO;
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
public class RunTestManageApi {

    private final RuleFlowExcuteService ruleFlowExcuteService;

    private final DebugRealTimeDataManager debugRealTimeDataManager;


    /**
     * 测试执行
     *
     * @param request
     * @return
     */
    @PostMapping("/excute")
    public ResultVO<ExcuteRuleFlowResult> testExcute(@Validated @RequestBody RuleFlowTestExcuteVO request) {
        try {
            ExcuteRuleFlow createProcessRequest = new ExcuteRuleFlow();
            createProcessRequest.setRequestId(IdUtil.getSnowflakeNextIdStr());
            createProcessRequest.setVariables(request.getVariables());
            createProcessRequest.setKey(request.getKey());
            createProcessRequest.setContent(request.getContent());
            createProcessRequest.setSkipNodeIds(request.getSkipNodeIds());
            createProcessRequest.setAsyncExcute(true);
            createProcessRequest.setDebugModel(true);
            return ResultVO.success(ruleFlowExcuteService.startFlow(createProcessRequest));
        } catch (Exception e) {
            log.error("执行出错：{}", e.getMessage(), e);
            return ResultVO.fail(e.getMessage());
        }
    }


    @PostMapping("/track")
    public ResultVO<ExcuteRuleFlowResult> track(@Validated @RequestBody IDRequestVO request) {
        return ResultVO.success(debugRealTimeDataManager.get(request.getId()));
    }
}