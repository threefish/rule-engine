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
package cn.xjbpm.rule.engine.runtime.actor;

import cn.xjbpm.rule.engine.definition.model.Node;
import cn.xjbpm.rule.engine.definition.model.ProcessModel;
import cn.xjbpm.rule.engine.runtime.model.FlowContext;
import lombok.Value;

import java.util.Collections;
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
        ProcessModel processModel;
        FlowContext flowContext;
        Set<String> executedNodeIds;

        public StartProcess(ProcessModel processModel, FlowContext flowContext, Set<String> executedNodeIds) {
            this.processModel = processModel;
            this.flowContext = flowContext;
            this.executedNodeIds = executedNodeIds == null ? Collections.emptySet() : executedNodeIds;
        }
    }

    // ================= Master -> Worker =================
    @Value
    class ExecuteNode {
        Node node;
        int attempt; // 当前是第几次尝试 (0开始)
        long startTotalTime;//记录该节点被调度的初始时间
    }

    // ================= Worker -> Master =================
    @Value
    class NodeCompleted {
        String nodeId;
        Node node;
        boolean success;
    }

    @Value
    class NodeFailed {
        String nodeId;
        Node node;
        Throwable reason;
    }
}