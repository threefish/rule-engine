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
