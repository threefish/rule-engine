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

package cn.xjbpm.rule.engine.runtime.actor;

import cn.xjbpm.rule.engine.definition.model.RuleFlowModel;
import cn.xjbpm.rule.engine.definition.model.nodes.Node;
import cn.xjbpm.rule.engine.runtime.model.FlowContext;
import lombok.Value;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Actor 消息协议定义
 *
 * @author 黄川 huchuc@vip.qq.com
 */
public interface WorkflowProtocol {

    // ================= 外部 -> Master =================
    @Value
    class StartProcess {
        RuleFlowModel ruleFlowModel;
        FlowContext flowContext;
        Set<String> skipNodeIds;

        public StartProcess(RuleFlowModel ruleFlowModel, FlowContext flowContext, Set<String> skipNodeIds) {
            this.ruleFlowModel = ruleFlowModel;
            this.flowContext = flowContext;
            this.skipNodeIds = skipNodeIds == null ? Collections.emptySet() : skipNodeIds;
        }
    }

    // ================= Master -> Worker =================
    @Value
    class ExecuteNode {
        Node node;
        int attempt; // 当前是第几次尝试 (0开始)
        long startTotalTime;//记录该节点被调度的初始时间
        FlowContext flowContext;
        ExecutionScope scope;
    }

    // ================= Worker -> Master =================
    @Value
    class NodeCompleted {
        String nodeId;
        Node node;
        boolean success;
        ExecutionScope scope;
        Long startTime;
    }

    @Value
    class NodeFailed {
        String nodeId;
        Node node;
        Throwable reason;
    }

    @Value
    class ScheduleNextLoopIteration {
        String loopNodeId;
        int nextIndex;
        Object nextItem;
        long startTime;
    }

    @Value
    @lombok.Builder
    class ExecutionScope implements java.io.Serializable {
        String loopNodeId;      // 循环节点ID
        String iterationId;     // 迭代唯一标识 (e.g., LoopID_Index)
        int index;              // 索引
        Map<String, Object> localVariables; // 局部变量
        long startTime;//记录该节点被调度的初始时间
    }
}