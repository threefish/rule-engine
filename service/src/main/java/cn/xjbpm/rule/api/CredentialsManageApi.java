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

import cn.xjbpm.rule.common.utils.StringUtils;
import cn.xjbpm.rule.repository.entity.CredentialsEntity;
import cn.xjbpm.rule.repository.enums.CredentialsType;
import cn.xjbpm.rule.service.CredentialsService;
import cn.xjbpm.rule.utils.SensitiveDataUtil;
import cn.xjbpm.rule.vo.CredentialsVO;
import cn.xjbpm.rule.vo.common.IDRequestVO;
import cn.xjbpm.rule.vo.common.OptionsVO;
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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author 黄川 huchuc@vip.qq.com
 */
@RestController()
@RequestMapping("/manage/ruleflow/credentials")
@RequiredArgsConstructor
public class CredentialsManageApi {

    private final CredentialsService credentialsService;

    @PostMapping("/page")
    public ResultVO<PageVO<CredentialsVO.PageResponse>> page(Pageable pageable, @RequestBody CredentialsVO.PageQuery query) {
        Page<CredentialsEntity> page = credentialsService.findPage(query, pageable);
        List<CredentialsVO.PageResponse> voList = page.getContent().stream()
                .map(credentialsEntity -> CredentialsVO.PageResponse.createPageView(credentialsEntity))
                .collect(Collectors.toList());
        return ResultVO.success(PageVO.of(page, voList));
    }

    @PostMapping("/details")
    public ResultVO<CredentialsVO.Details> details(@Validated @RequestBody IDRequestVO request) {
        CredentialsVO.Details details = credentialsService.details(request.getId());
        // 不返回原始信息
        details.setAuthData(null);
        details.setAuthDataMap(SensitiveDataUtil.maskSensitiveData(details.getAuthDataMap()));
        return ResultVO.success(details);
    }

    @PostMapping("/save")
    public ResultVO<String> save(@Validated @RequestBody CredentialsVO.Save request) {
        return ResultVO.success(String.valueOf(credentialsService.save(request)));
    }

    @PostMapping("/options")
    public ResultVO<List<OptionsVO>> options(@Validated @RequestBody CredentialsVO.SelectQuery request) {
        if (StringUtils.isNotBlank(request.getType())) {
            if (Objects.equals("http", request.getType())) {
                request.setTypes(Arrays.asList(CredentialsType.basic_auth, CredentialsType.bearer_auth, CredentialsType.header_auth));
            } else if (Objects.equals("ssh", request.getType())) {
                request.setTypes(Arrays.asList(CredentialsType.shh_password, CredentialsType.shh_private_key));
            } else if (Objects.equals("deepseek", request.getType())) {
                request.setTypes(Arrays.asList(CredentialsType.deepseek));
            } else if (Objects.equals("volcengine", request.getType())) {
                request.setTypes(Arrays.asList(CredentialsType.volcengine));
            }
        }
        return ResultVO.success(credentialsService.options(request));
    }
}