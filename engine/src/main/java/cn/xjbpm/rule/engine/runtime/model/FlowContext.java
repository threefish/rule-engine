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

import cn.xjbpm.rule.common.utils.StringUtils;
import cn.xjbpm.rule.custom.BeanContextManager;
import cn.xjbpm.rule.dto.ExcuteRuleFlowResult;
import cn.xjbpm.rule.engine.aviator.SmartEnvMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 规则流执行上下文：用于在多个节点间共享数据
 * 提供线程安全的数据访问机制
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Slf4j
public class FlowContext {

    @Getter
    private final BeanContextManager beanContextManager;

    @Getter
    private final boolean debugModel;

    @Getter
    private final ExcuteRuleFlowResult processInstance;

    /**
     * 业务入参变量
     */
    private final Map<String, Object> variable;

    /**
     * 节点执行信息
     */
    private final Map<String, NodeExcution> nodeExcutions = new ConcurrentHashMap<>();

    /**
     * 记录执行日志
     */
    private final Collection<TraceLog> traceLogs = new ConcurrentLinkedQueue<>();

    public FlowContext(ExcuteRuleFlowResult processInstance, Map<String, Object> variable, boolean debugModel, BeanContextManager beanContextManager) {
        this.processInstance = processInstance;
        this.variable = new SmartEnvMap(variable);
        this.debugModel = debugModel;
        this.beanContextManager = beanContextManager;
    }

    public Collection<TraceLog> getTraceLogs() {
        return traceLogs;
    }

    public Map<String, Object> getNodesData() {
        Map<String, Object> nodesData = new HashMap<>(nodeExcutions.size());
        this.getVariable().forEach((key, value) -> {
            if (key.startsWith("N")) {
                nodesData.put(key, value);
            }
        });
        return nodesData;
    }

    public Map<String, Object> getVariable() {
        return variable;
    }

    public void putNodeExcution(String key, NodeExcution excution) {
        nodeExcutions.put(key, excution);
    }

    public Map<String, NodeExcution> getNodeExcutions() {
        return nodeExcutions;
    }

    /**
     * 存储节点数据出参，提供给后续节点使用数据
     */
    public void put(String nodeId, Object value) {
        if (value == null) {
            return;
        }
        variable.put(nodeId, value);
    }

    public Object get(String nodeId) {
        return variable.get(nodeId);
    }

    public void addTraceLog(String nodeId, String message, Object... args) {
        traceLogs.add(TraceLog.of(nodeId, StringUtils.format(message, args)));
    }

}