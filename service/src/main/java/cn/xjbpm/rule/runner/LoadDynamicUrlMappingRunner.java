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

package cn.xjbpm.rule.runner;

import cn.xjbpm.rule.repository.entity.DynamicUrlMappingEntity;
import cn.xjbpm.rule.service.DynamicUrlMappingService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author 黄川 huchuc@vip.qq.com
 * 启动时加载动态URL映射
 */
@Service
@Slf4j
@AllArgsConstructor
public class LoadDynamicUrlMappingRunner implements CommandLineRunner {

    private final DynamicUrlMappingService dynamicUrlMappingService;

    @Override
    public void run(String... args) {
        try {
            log.info("开始加载动态URL映射...");
            List<DynamicUrlMappingEntity> enabledMappings = dynamicUrlMappingService.findAllEnabled();
            log.info("发现 {} 个已启用的动态URL映射", enabledMappings.size());
            
            for (DynamicUrlMappingEntity mapping : enabledMappings) {
                dynamicUrlMappingService.registerMappingOnStartup(mapping);
            }
            
            log.info("加载动态URL映射完成，成功注册 {} 个映射", enabledMappings.size());
        } catch (Exception e) {
            log.error("加载动态URL映射失败", e);
        }
    }

}
