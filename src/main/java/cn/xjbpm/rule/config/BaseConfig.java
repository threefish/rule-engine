package cn.xjbpm.rule.config;

import cn.xjbpm.rule.engine.definition.DefaultProcessDefinitionCache;
import cn.xjbpm.rule.engine.definition.ProcessDefinitionCache;
import org.nutz.dao.spring.boot.annotation.DaoScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/7/22
 */
@Configuration
@DaoScan("cn.xjbpm.rule.engine.persistence")
public class BaseConfig {

    @Bean
    public ProcessDefinitionCache processDefinitionCache() {
        return new DefaultProcessDefinitionCache();
    }
}
