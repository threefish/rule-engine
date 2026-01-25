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
     * 默认执行超时时间
     */
    private long akkaDefaultExecuteTimeoutSeconds = 30;
    /**
     * 全局 Worker 路由池初始大小
     */
    private int akkaGlobalWorkerPoolInitSize = 200;
    /**
     * 全局 Worker 路由池最大大小
     */
    private int akkaGlobalWorkerPoolMaxSize = 2000;


}
