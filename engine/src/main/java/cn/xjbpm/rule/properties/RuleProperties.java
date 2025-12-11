package cn.xjbpm.rule.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2025/12/11
 */
@Configuration
@ConfigurationProperties(prefix = "rule")
@Data
public class RuleProperties {

    /**
     * 名称
     */
    private String akkaSystemName = "rule-engine";

    /**
     * 默认超时时间
     */
    private long akkaDefaultTimeoutSeconds = 30;
    /**
     * 全局 Worker 线程池大小
     */
    private int akkaGlobalWorkerPoolSize = 200;


}
