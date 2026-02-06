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

import cn.xjbpm.rule.common.utils.JsonUtils;
import cn.xjbpm.rule.repository.entity.AuthoriztionEntity;
import cn.xjbpm.rule.service.AuthoriztionService;
import cn.xjbpm.rule.vo.AuthoriztionVO;
import cn.xjbpm.rule.vo.common.IDRequestVO;
import cn.xjbpm.rule.vo.common.PageVO;
import cn.xjbpm.rule.vo.common.ResultVO;
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
public class AuthoriztionManageApi {

    private final AuthoriztionService authoriztionService;

    @PostMapping("/page")
    public ResultVO<PageVO<AuthoriztionEntity>> page(Pageable pageable, @RequestBody AuthoriztionVO.PageQuery query) {
        Page<AuthoriztionEntity> page = authoriztionService.findPage(query.getApiKey(), pageable);
        return ResultVO.success(PageVO.of(page));
    }

    @PostMapping("/save")
    public ResultVO<Boolean> save(@Validated @RequestBody AuthoriztionVO.SaveRequest request) {
        try {
            List<String> list = JsonUtils.json2List(request.getAuthoriztion(), String.class);
            request.setAuthoriztion(JsonUtils.obj2Json(list));
        } catch (Exception e) {
            return ResultVO.fail("授权规则格式错误，必须是JSON数组格式！");
        }
        return ResultVO.success(authoriztionService.save(request));
    }


    @PostMapping("/disable")
    public ResultVO<Boolean> disable(@Validated @RequestBody IDRequestVO request) {
        return ResultVO.success(authoriztionService.changeStatus(request.getId(), false));
    }

    @PostMapping("/enable")
    public ResultVO<Boolean> enable(@Validated @RequestBody IDRequestVO request) {
        return ResultVO.success(authoriztionService.changeStatus(request.getId(), true));
    }

}