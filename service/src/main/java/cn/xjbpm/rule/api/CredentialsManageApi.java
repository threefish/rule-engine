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
        String type = request.getType();
        if (StringUtils.isNotBlank(type)) {
            List<CredentialsType> types = switch (type) {
                case "http" -> Arrays.asList(CredentialsType.basic_auth, CredentialsType.bearer_auth, CredentialsType.header_auth);
                case "ssh" -> Arrays.asList(CredentialsType.shh_password, CredentialsType.shh_private_key);
                case "aitext" -> Arrays.asList(CredentialsType.volcengine, CredentialsType.deepseek);
                case "aiimage" -> Arrays.asList(CredentialsType.volcengine, CredentialsType.gemini);
                case "aitts" -> List.of(CredentialsType.gemini);
                case "db" -> List.of(CredentialsType.db);
                case "ftp" -> List.of(CredentialsType.ftp);
                case "email" -> List.of(CredentialsType.email);
                case "redis" -> List.of(CredentialsType.redis);
                case "mqtt" -> List.of(CredentialsType.mqtt);
                case "rabbitmq" -> List.of(CredentialsType.rabbitmq);
                case "kafka" -> List.of(CredentialsType.kafka);
                default -> request.getTypes();
            };
            request.setTypes(types);
        }
        return ResultVO.success(credentialsService.options(request));
    }

    @PostMapping("/type")
    public ResultVO<CredentialsType> options(@Validated @RequestBody IDRequestVO request) {
        return ResultVO.success(credentialsService.type(request.getId()));
    }
}