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

package cn.xjbpm.rule.engine.runtime.model;

/**
 * @author 黄川 huchuc@vip.qq.com
 */
public enum ExecutStatus {

    /**
     * 节点执行中
     */
    RUNNING,
    /**
     * 节点执行成功
     */
    SUCCESS,
    /**
     * 节点执行异常
     */
    FAILURE,
    /**
     * 引擎内部执行跳过状态，非用户指定执行跳过状态
     */
    SKIP,
    /**
     * 节点已到达过，正在等待中其他节点执行完成触发完成检测
     */
    WAITING
}