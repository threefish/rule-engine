package cn.xjbpm.rule.config;

import cn.xjbpm.rule.RuleEngineApplication;
import cn.xjbpm.rule.engine.common.utils.IdGenerUtils;
import cn.xjbpm.rule.engine.definition.DefaultProcessDefinitionCache;
import cn.xjbpm.rule.engine.definition.ProcessDefinitionCache;
import org.nutz.dao.enhance.incrementer.IdentifierGenerator;
import org.nutz.dao.spring.boot.annotation.DaoScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/7/22
 */
@Configuration
@DaoScan(basePackageClasses = RuleEngineApplication.class)
public class BaseConfig {

    @Bean
    public ProcessDefinitionCache processDefinitionCache() {
        return new DefaultProcessDefinitionCache();
    }

    @Bean
    public IdentifierGenerator identifierGenerator() {
        return entity -> IdGenerUtils.nextGlobalId();
    }
}
