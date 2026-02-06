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

import cn.xjbpm.rule.properties.RuleServiceProperties;
import cn.xjbpm.rule.utils.FieldUtil;
import cn.xjbpm.rule.utils.JwtUtil;
import cn.xjbpm.rule.vo.LoginVO;
import cn.xjbpm.rule.vo.common.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 */
@RestController
@RequestMapping("/manage")
@RequiredArgsConstructor
public class LoginManageApi {

    private final RuleServiceProperties ruleServiceProperties;

    @PostMapping("/login")
    public ResultVO<LoginVO.Response> login(@Validated @RequestBody LoginVO.Request request) {
        if (!ruleServiceProperties.getAdminAccount().equals(request.getUsername()) || !ruleServiceProperties.getAdminPassword().equals(request.getPassword())) {
            return ResultVO.fail("账号或密码错误！");
        }
        Map<String, Object> claims = new HashMap<>();
        claims.put(FieldUtil.name(LoginVO.Request::getUsername), request.getUsername());
        String token = JwtUtil.createToken(claims, ruleServiceProperties.getJwtSecret());
        return ResultVO.success(LoginVO.Response.builder().token(token).username(request.getUsername()).build());
    }

}