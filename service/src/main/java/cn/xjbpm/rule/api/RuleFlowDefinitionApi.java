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
import cn.xjbpm.rule.vo.IDRequestVO;
import cn.xjbpm.rule.vo.ResultVO;
import cn.xjbpm.rule.vo.RuleFlowPageQuery;
import cn.xjbpm.rule.vo.RuleFlowVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public ResultVO<String> save(@Validated @RequestBody RuleFlowVO request) {
        if (request == null || request.getKey() == null) {
            throw new IllegalArgumentException("规则编码不可为空！");
        }
        processDefinitionService.convertToModel(request.getContent());
        RuleFlowEntity ruleFlowEntity = new RuleFlowEntity();
        ruleFlowEntity.setId(request.getId());
        ruleFlowEntity.setKey(request.getKey());
        ruleFlowEntity.setName(request.getName());
        ruleFlowEntity.setDescription(request.getDescription());
        ruleFlowEntity.setContent(request.getContent());
        ruleFlowEntity.setVersion(request.getVersion());
        RuleFlowEntity entity = ruleFlowService.saveOrUpdate(ruleFlowEntity);
        return ResultVO.success(String.valueOf(entity.getId()));
    }

    @PostMapping("/get")
    public ResultVO<RuleFlowVO> save(@Validated @RequestBody IDRequestVO request) {
        RuleFlowEntity entity = ruleFlowService.findById(request.getId());
        if (entity == null) {
            return ResultVO.fail("未找到规则定义");
        }
        RuleFlowVO vo = new RuleFlowVO();
        vo.setId(entity.getId());
        vo.setKey(entity.getKey());
        vo.setName(entity.getName());
        vo.setDescription(entity.getDescription());
        vo.setContent(entity.getContent());
        vo.setVersion(entity.getVersion());
        return ResultVO.success(vo);
    }

    @PostMapping("/page")
    public ResultVO<Page<RuleFlowEntity>> findRuleFlowPage(Pageable pageable, @RequestBody RuleFlowPageQuery query) {
        return ResultVO.success(ruleFlowService.findPage(pageable, query.getKey(), query.getName()));
    }
}