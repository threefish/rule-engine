/*
 * Copyright 2025 threefish.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.xjbpm.rule.engine.runtime.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 流程执行上下文：用于在多个节点间共享数据
 * 提供线程安全的数据访问机制
 *
 * @author 黄川 huchuc@vip.qq.com
 */
public class FlowContext {

    /**
     * 节点共享数据
     */

    private final Map<String, Object> sharedData = new ConcurrentHashMap<>();

    /**
     * 业务入参变量
     */
    private final Map<String, Object> variable;

    /**
     * 节点执行信息
     */
    private final Map<String, NodeExcution> nodeExcutions = new ConcurrentHashMap<>();
    /**
     * 调试模式下，记录日志
     */
    private final List<TraceLog> traceLogs = new ArrayList<>();

    public FlowContext(Map<String, Object> variable) {
        this.variable = new ConcurrentHashMap<>(variable);
    }

    public List<TraceLog> getTraceLogs() {
        return traceLogs;
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
     * 存储共享数据
     */
    public void put(String key, Object value) {
        sharedData.put(key, value);
    }

    /**
     * 获取共享数据
     */
    public Object get(String key) {
        return sharedData.get(key);
    }

    /**
     * 获取指定类型的共享数据
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        Object value = sharedData.get(key);
        return value != null ? (T) value : null;
    }

    /**
     * 移除共享数据
     */
    public Object remove(String key) {
        return sharedData.remove(key);
    }

    /**
     * 检查是否包含指定键
     */
    public boolean containsKey(String key) {
        return sharedData.containsKey(key);
    }

    /**
     * 清空所有共享数据
     */
    public void clear() {
        sharedData.clear();
    }

    /**
     * 获取所有共享数据的快照
     */
    public Map<String, Object> getAllData() {
        return new ConcurrentHashMap<>(sharedData);
    }

    public void addTraceLog(String message) {
        traceLogs.add(TraceLog.of(message));
    }
}