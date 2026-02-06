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

package cn.xjbpm.rule;

import cn.xjbpm.rule.utils.ApplicationWebPathUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * @author 黄川 huchuc@vip.qq.com
 */
@EnableJpaAuditing
@SpringBootApplication
@Slf4j
@SuppressWarnings("all")
public class RuleEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(RuleEngineApplication.class, args);
    }


    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReadyEvent() {
        String appRootPathUrl = ApplicationWebPathUtil.getAppRootPathUrl();
        log.info("######################################################");
        log.info(appRootPathUrl);
        log.info("Application started                                   ");
        log.info("######################################################");
    }

    @EventListener(ContextClosedEvent.class)
    public void onContextClosedEvent() {
        log.info("                                                      ");
        log.info("######################################################");
        log.info("Application is closed                                 ");
        log.info("######################################################");
    }

}