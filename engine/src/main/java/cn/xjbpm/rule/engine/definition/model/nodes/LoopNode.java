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

package cn.xjbpm.rule.engine.definition.model.nodes;

import cn.xjbpm.rule.engine.definition.model.enums.NodeType;
import cn.xjbpm.rule.engine.runtime.actor.NodeDependencyBuilder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/28
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LoopNode extends Node {

    /**
     * FlowContext中集合变量的名称 (List<Object>)
     */
    private String collectionVariableName;

    /**
     * 遍历时单个元素的变量名 (放入子流程Context中)
     */
    private String loopRowVariableName;
    private String loopRowIndex;
    /**
     * 循环间隔时间
     */
    private Long delayTime;

    /**
     * 子流程的依赖关系构建器
     * (包含子流程的 nodeNextMap 和 convergePendingCount)
     */
    private transient NodeDependencyBuilder childGraph;

    /**
     * 辅助字段：存储子节点的起始节点（通常是子流程中入度为0的节点）
     */
    private transient List<Node> startNodes;

    @Override
    public NodeType getType() {
        return NodeType.LoopNode;
    }


}