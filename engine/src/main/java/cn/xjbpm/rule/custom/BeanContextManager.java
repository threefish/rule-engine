package cn.xjbpm.rule.custom;

import org.springframework.context.ApplicationEventPublisher;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2025/12/18
 */
public interface BeanContextManager {
    CredentialsManager getCredentialsManager();
    ApplicationEventPublisher getEventPublishManager();

}
