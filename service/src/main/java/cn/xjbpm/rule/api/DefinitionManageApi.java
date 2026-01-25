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

import cn.xjbpm.rule.custom.RuleFlowModelCacheManager;
import cn.xjbpm.rule.repository.entity.RuleFlowEntity;
import cn.xjbpm.rule.service.RuleFlowService;
import cn.xjbpm.rule.vo.RuleFlowVO;
import cn.xjbpm.rule.vo.common.IDRequestVO;
import cn.xjbpm.rule.vo.common.KeyRequestVO;
import cn.xjbpm.rule.vo.common.PageVO;
import cn.xjbpm.rule.vo.common.ResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 黄川 huchuc@vip.qq.com
 */
@RestController
@RequestMapping("/manage/ruleflow")
@RequiredArgsConstructor
@Slf4j
public class DefinitionManageApi {

    private final RuleFlowService ruleFlowService;

    private final RuleFlowModelCacheManager ruleFlowModelCacheManager;

    @PostMapping("/add")
    public ResultVO<Boolean> add(@Validated @RequestBody RuleFlowVO request) {
        return ResultVO.success(ruleFlowService.add(request));
    }

    @PostMapping("/save")
    public ResultVO<Boolean> save(@Validated @RequestBody RuleFlowVO request) {
        ruleFlowModelCacheManager.convertToModel(request.getDraftContent());
        return ResultVO.success(ruleFlowService.update(request));
    }

    @PostMapping("/deploy")
    public ResultVO<Boolean> deploy(@Validated @RequestBody IDRequestVO request) {
        return ResultVO.success(ruleFlowService.deployById(request.getId(), true));
    }

    @PostMapping("/disable")
    public ResultVO<Boolean> disable(@Validated @RequestBody IDRequestVO request) {
        return ResultVO.success(ruleFlowService.disableById(request.getId()));
    }

    @PostMapping("/enable")
    public ResultVO<Boolean> enable(@Validated @RequestBody IDRequestVO request) {
        return ResultVO.success(ruleFlowService.deployById(request.getId(), false));
    }

    @PostMapping("/get")
    public ResultVO<RuleFlowVO> get(@Validated @RequestBody KeyRequestVO request) {
        return ResultVO.success(ruleFlowService.findByKey(request.getKey()));
    }

    @PostMapping("/page")
    public ResultVO<PageVO<RuleFlowVO>> page(Pageable pageable, @RequestBody RuleFlowVO.PageQuery query) {
        Page<RuleFlowEntity> page = ruleFlowService.findPage(query.getKey(), query.getName(), query.getStatus(), pageable);
        List<RuleFlowVO> voList = page.getContent().stream().map(RuleFlowVO::createPageView).collect(Collectors.toList());
        return ResultVO.success(PageVO.of(page, voList));
    }

}