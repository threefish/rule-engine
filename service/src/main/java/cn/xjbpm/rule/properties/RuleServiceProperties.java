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
package cn.xjbpm.rule.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2025/12/11
 */
@Configuration
@ConfigurationProperties(prefix = "service")
@Data
@Validated
public class RuleServiceProperties {

    /**
     * 演示模式
     */
    private Boolean demoMode = false;

    @NotBlank
    @Length(min = 6, max = 16)
    private String adminAccount = "superadmin";

    @NotBlank
    @Length(min = 6, max = 16)
    private String adminPassword = "superadmin";

    @Length(min = 32, max = 64)
    private String jwtSecret = "h2g24j#1asda2g2g24j#1asda2g24j#1asda";

}