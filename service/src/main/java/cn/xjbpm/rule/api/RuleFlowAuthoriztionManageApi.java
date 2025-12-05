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
import cn.xjbpm.rule.repository.entity.RuleFlowAuthoriztionEntity;
import cn.xjbpm.rule.service.RuleFlowAuthoriztionService;
import cn.xjbpm.rule.vo.RuleFlowAuthoriztionVO;
import cn.xjbpm.rule.vo.common.IDRequestVO;
import cn.xjbpm.rule.vo.common.PageVO;
import cn.xjbpm.rule.vo.common.ResultVO;
import cn.xjbpm.rule.vo.query.RuleFlowAuthoriztionPageQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author 黄川 huchuc@vip.qq.com
 */
@RestController()
@RequestMapping("/manage/ruleflow/authoriztion")
@RequiredArgsConstructor
public class RuleFlowAuthoriztionManageApi {

    private final RuleFlowAuthoriztionService ruleFlowAuthoriztionService;

    @PostMapping("/page")
    public ResultVO<PageVO<RuleFlowAuthoriztionEntity>> page(Pageable pageable, @RequestBody RuleFlowAuthoriztionPageQuery query) {
        Page<RuleFlowAuthoriztionEntity> page = ruleFlowAuthoriztionService.findPage(query.getAppCode(), pageable);
        page.getContent().forEach(entity -> entity.setSecretKey( null));
        return ResultVO.success(PageVO.of(page));
    }

    @PostMapping("/save")
    public ResultVO<RuleFlowAuthoriztionVO.AddResponse> save(@Validated @RequestBody RuleFlowAuthoriztionVO.SaveRequest request) {
        try {
            List<String> list = JsonUtils.json2List(request.getAuthoriztion(), String.class);
            request.setAuthoriztion(JsonUtils.obj2Json(list));
        } catch (Exception e) {
            return ResultVO.fail("授权规则格式错误，必须是JSON数组格式！");
        }
        return ResultVO.success(ruleFlowAuthoriztionService.save(request));
    }


    @PostMapping("/resetKey")
    public ResultVO<RuleFlowAuthoriztionVO.AddResponse> resetKey(@Validated @RequestBody IDRequestVO request) {
        return ResultVO.success(ruleFlowAuthoriztionService.resetKey(request.getId()));
    }

    @PostMapping("/disable")
    public ResultVO<Boolean> disable(@Validated @RequestBody IDRequestVO request) {
        return ResultVO.success(ruleFlowAuthoriztionService.changeStatus(request.getId(), false));
    }

    @PostMapping("/enable")
    public ResultVO<Boolean> enable(@Validated @RequestBody IDRequestVO request) {
        return ResultVO.success(ruleFlowAuthoriztionService.changeStatus(request.getId(), true));
    }

}