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

import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * 节点执行结果缓存
 */
public interface NodeExcutionPinnedCacheManager {
    /**
     * 获取节点最后执行结果
     *
     * @param ruleFlowKey
     * @param NodeId
     * @return
     */
    Map getNodeLatestPinnedResult(String ruleFlowKey, String NodeId);

    /**
     * 设置节点最后执行结果
     *
     * @param ruleFlowKey
     * @param id
     * @param object
     */
    void setNodeLatestPinnedResult(String ruleFlowKey, String id, Object object);
}
