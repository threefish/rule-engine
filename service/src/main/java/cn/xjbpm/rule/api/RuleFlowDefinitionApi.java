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

import cn.xjbpm.rule.custom.RuleFlowDefinitionService;
import cn.xjbpm.rule.repository.entity.RuleFlowEntity;
import cn.xjbpm.rule.service.RuleFlowService;
import cn.xjbpm.rule.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 黄川 huchuc@vip.qq.com
 */
@RestController()
@RequestMapping("/manage/ruleflow")
@RequiredArgsConstructor
public class RuleFlowDefinitionApi {

    private final RuleFlowService ruleFlowService;

    private final RuleFlowDefinitionService processDefinitionService;

    @PostMapping("/save")
    public ResultVO<Long> save(@Validated @RequestBody RuleFlowVO request) {
        if (request == null || request.getKey() == null) {
            throw new IllegalArgumentException("规则编码不可为空！");
        }
        processDefinitionService.convertToModel(request.getContent());
        Long id;
        if (request.getNewVersion() == true) {
            Assert.notNull(request.getId(), "ID不能为空！");
            id = ruleFlowService.saveNewVersion(request);
        } else {
            id = ruleFlowService.saveOrUpdateNoDeployed(request);
        }
        return ResultVO.success(id);
    }

    @PostMapping("/deploy")
    public ResultVO<Boolean> deploy(@Validated @RequestBody IDRequestVO request) {
        return ResultVO.success(ruleFlowService.deploy(request.getId()));
    }

    @PostMapping("/paused")
    public ResultVO<Boolean> paused(@Validated @RequestBody IDRequestVO request) {
        return ResultVO.success(ruleFlowService.paused(request.getId()));
    }

    @PostMapping("/get")
    public ResultVO<RuleFlowVO> get(@Validated @RequestBody IDRequestVO request) {
        return ResultVO.success(ruleFlowService.findById(request.getId()));
    }

    @PostMapping("/page")
    public ResultVO<PageVO<RuleFlowEntity>> findRuleFlowPage(Pageable pageable, @RequestBody RuleFlowPageQuery query) {
        Page<RuleFlowEntity> page = ruleFlowService.findPage(query.getKey(), query.getName(), query.getStatus(), pageable);
        return ResultVO.success(PageVO.of(page));
    }
}