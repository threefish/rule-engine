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
import cn.xjbpm.rule.custom.EngineServices;
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
 * 规则流执行上下文：在整个规则流的所有节点间共享状态的纯数据对象。
 *
 * <p>职责分区：
 * <ul>
 *   <li><b>标识</b>：{@link #processInstance}、{@link #debugModel} — 描述本次执行的身份和模式</li>
 *   <li><b>引擎服务</b>：{@link #engineServices} — 提供凭据、缓存等运行时所需服务；
 *       不含任何 Spring 依赖，由 service 层注入</li>
 *   <li><b>变量存储</b>：{@link #variable} — Aviator 表达式运行环境，包含入参变量及赋值节点写入的变量；
 *       通过 {@link #getVariable()} 暴露给行为类和表达式引擎</li>
 *   <li><b>节点输出</b>：{@link #setNodeOutput}/{@link #getNodeOutput}/{@link #getNodesData} —
 *       各节点将自身执行结果以节点 ID 为键写入，后续节点可通过表达式引用</li>
 *   <li><b>执行追踪</b>：{@link #nodeExcutions} — 记录每个节点的执行状态（耗时、成功/失败等）</li>
 *   <li><b>可观测性</b>：{@link #traceLogs} — 执行过程追踪日志</li>
 * </ul>
 *
 * <p><b>设计约束</b>：本类不引入任何 Spring 框架类型，保持引擎核心对框架无感知。
 * 调试事件发布等 Spring 相关操作由外部通过回调（{@code Consumer<FlowContext>}）注入 Actor 层处理。
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Slf4j
public class FlowContext implements java.io.Serializable {

    /**
     * 引擎运行时所需服务（凭据、缓存、配置），无 Spring 依赖。
     * 由 service 层在创建 FlowContext 时注入，行为类通过 {@link #getEngineServices()} 获取。
     */
    @Getter
    private final EngineServices engineServices;

    /**
     * 是否开启调试模式。调试模式下每次节点状态变更都会触发外部注入的调试回调。
     */
    @Getter
    private final boolean debugModel;

    /**
     * 本次规则流执行的结果对象，贯穿整个执行周期，最终返回给调用方。
     */
    @Getter
    private final ExcuteRuleFlowResult processInstance;

    /**
     * 运行时变量表：Aviator 表达式的执行环境。
     *
     * <p>包含：
     * <ul>
     *   <li>调用方传入的业务入参</li>
     *   <li>赋值节点（AssignmentNode）写入的变量</li>
     *   <li>各节点通过 {@link #setNodeOutput} 写入的输出（以节点 ID 为键）</li>
     *   <li>循环节点的迭代变量（当前行、当前索引等）</li>
     * </ul>
     *
     * <p>直接暴露给行为类，以便它们向 Aviator 写入赋值变量或读取已有变量。
     * 若仅需读写节点输出，请优先使用 {@link #setNodeOutput}/{@link #getNodeOutput}。
     */
    private final Map<String, Object> variable;

    /**
     * 各节点的执行信息（状态、耗时、错误信息等）。
     * Key 为节点 ID。
     */
    private final Map<String, NodeExcution> nodeExcutions = new ConcurrentHashMap<>();

    /**
     * 执行追踪日志，按时间顺序记录节点执行过程中的关键事件。
     */
    private final Collection<TraceLog> traceLogs = new ConcurrentLinkedQueue<>();

    public FlowContext(ExcuteRuleFlowResult processInstance, Map<String, Object> variable,
                       boolean debugModel, EngineServices engineServices) {
        this.processInstance = processInstance;
        this.variable = new SmartEnvMap(variable);
        this.debugModel = debugModel;
        this.engineServices = engineServices;
    }

    // -------------------------------------------------------------------------
    // 变量存储
    // -------------------------------------------------------------------------

    /**
     * 返回运行时变量表，供 Aviator 表达式引擎和行为类直接读写。
     *
     * <p>注意：若只需写入某节点的执行结果，请使用 {@link #setNodeOutput}；
     * 若只需删除某个键，请使用 {@link #removeVariable}，以保持语义清晰。
     */
    public Map<String, Object> getVariable() {
        return variable;
    }

    /**
     * 从变量表中删除指定键。
     *
     * <p>典型用途：循环节点执行完毕后清理迭代变量。
     *
     * @param key 要删除的变量键
     */
    public void removeVariable(String key) {
        variable.remove(key);
    }

    // -------------------------------------------------------------------------
    // 节点输出
    // -------------------------------------------------------------------------

    /**
     * 将节点的执行输出写入变量表，以 {@code nodeId} 为键。
     *
     * <p>写入后，后续节点可在 Aviator 表达式中通过节点 ID 引用该输出，
     * 例如 {@code N001.result}。
     *
     * @param nodeId 节点 ID
     * @param value  节点输出（为 {@code null} 时忽略）
     */
    public void setNodeOutput(String nodeId, Object value) {
        if (value == null) {
            return;
        }
        variable.put(nodeId, value);
    }

    /**
     * 获取指定节点的执行输出。
     *
     * @param nodeId 节点 ID
     * @return 节点输出，若节点尚未执行或无输出则为 {@code null}
     */
    public Object getNodeOutput(String nodeId) {
        return variable.get(nodeId);
    }

    /**
     * 返回所有已执行节点的输出快照（Key = 节点 ID，Value = 节点输出）。
     *
     * <p>仅包含已在 {@link #nodeExcutions} 中登记且在变量表中存有输出的节点，
     * 不含纯业务入参变量。
     */
    public Map<String, Object> getNodesData() {
        Map<String, Object> nodesData = new HashMap<>(nodeExcutions.size());
        for (String nodeId : nodeExcutions.keySet()) {
            Object val = variable.get(nodeId);
            if (val != null) {
                nodesData.put(nodeId, val);
            }
        }
        return nodesData;
    }

    // -------------------------------------------------------------------------
    // 执行追踪
    // -------------------------------------------------------------------------

    /**
     * 记录节点执行信息（状态、耗时、错误信息等）。
     *
     * @param key      节点 ID
     * @param excution 节点执行信息
     */
    public void putNodeExcution(String key, NodeExcution excution) {
        nodeExcutions.put(key, excution);
    }

    /**
     * 返回所有节点的执行信息映射（Key = 节点 ID）。
     */
    public Map<String, NodeExcution> getNodeExcutions() {
        return nodeExcutions;
    }

    // -------------------------------------------------------------------------
    // 可观测性
    // -------------------------------------------------------------------------

    /**
     * 返回执行追踪日志集合。
     */
    public Collection<TraceLog> getTraceLogs() {
        return traceLogs;
    }

    /**
     * 追加一条追踪日志。
     *
     * @param nodeId  关联的节点 ID（可为 {@code null}）
     * @param message 日志消息模板（支持 {@code {}} 占位符）
     * @param args    消息参数
     */
    public void addTraceLog(String nodeId, String message, Object... args) {
        traceLogs.add(TraceLog.of(nodeId, StringUtils.format(message, args)));
    }
}
