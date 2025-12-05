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
package cn.xjbpm.rule.vo;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.util.List;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2025/12/4
 */
public class RuleFlowAuthoriztionVO {

    @Data
    public static class SaveRequest {
        @NotBlank
        @Length(max = 20, min = 1)
        String appCode;
        @NotBlank
        @Length(max = 20, min = 1)
        String name;
        @NotBlank
        @Length(max = 20, min = 1)
        String description;
        @NotBlank
        String authoriztion;
    }

    @Data
    public static class AddResponse {
        String appCode;
        String secretKey;
    }
}
