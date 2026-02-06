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

package cn.xjbpm.rule.listener;

import cn.xjbpm.rule.listener.event.DisabledApiKeyEvent;
import cn.xjbpm.rule.service.OpenApiAuthoriztionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * @author 黄川 huchuc@vip.qq.com
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DisabledApiKeyEventListener implements ApplicationListener<DisabledApiKeyEvent> {

    private final OpenApiAuthoriztionService openApiAuthoriztionService;

    @Override
    public void onApplicationEvent(DisabledApiKeyEvent event) {
        log.info("ApiKey 禁用事件 ApiKey:{}", event.getData());
        openApiAuthoriztionService.invalidate(event.getData());
    }
}