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

import cn.xjbpm.rule.repository.entity.RuleFlowExcuteLogEntity;
import cn.xjbpm.rule.service.RuleFlowExcuteLogService;
import cn.xjbpm.rule.vo.IDRequestVO;
import cn.xjbpm.rule.vo.ResultVO;
import cn.xjbpm.rule.vo.RuleFlowExcuteLogsPageQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 黄川 huchuc@vip.qq.com
 */
@RestController()
@RequestMapping("/manage/ruleflow/logs")
@RequiredArgsConstructor
public class RuleFlowExcuteLogsApi {

    private final RuleFlowExcuteLogService ruleFlowExcuteLogService;

    @PostMapping("/page")
    public ResultVO<Page<RuleFlowExcuteLogEntity>> findRuleFlowPage(Pageable pageable, @RequestBody RuleFlowExcuteLogsPageQuery query) {
        return ResultVO.success(ruleFlowExcuteLogService.findPage(pageable, query.getRuleFlowKey(), query.getRequestId()));
    }

    @PostMapping("/detail")
    public ResultVO<RuleFlowExcuteLogEntity> findRuleFlowDetail(@RequestBody IDRequestVO query) {
        return ResultVO.success(ruleFlowExcuteLogService.findById(query.getId()));
    }
}