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
package cn.xjbpm.rule.custom;

import cn.xjbpm.rule.properties.RuleProperties;

/**
 * 引擎运行时所需的基础服务抽象
 *
 * @author 黄川 huchuc@vip.qq.com
 */
public interface EngineServices {

    /**
     * 节点固定（Pinned）执行结果缓存管理器。
     */
    NodeExcutionPinnedCacheManager getNodePinnedCacheManager();

    /**
     * 凭据管理器，提供各类外部系统凭据（HTTP、数据库、MQ 等）。
     */
    CredentialsManager getCredentialsManager();

    /**
     * 规则引擎配置属性。
     */
    RuleProperties getRuleProperties();
}
