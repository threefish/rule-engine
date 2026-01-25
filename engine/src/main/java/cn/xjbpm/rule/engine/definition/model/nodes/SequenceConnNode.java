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
package cn.xjbpm.rule.engine.definition.model.nodes;

import cn.xjbpm.rule.engine.definition.model.enums.NodeType;
import cn.xjbpm.rule.engine.rule.Rule;
import lombok.Data;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/29
 */
@Data
public class SequenceConnNode extends Node {

    /**
     * 源节点key
     */
    protected String sourceNodeKey;

    /**
     * 目标节点key
     */
    protected String targetNodeKey;

    /**
     * 源节点
     */
    protected Node sourceNode;

    /**
     * 目标节点
     */
    protected Node targetNode;

    protected int sortNum;

    protected Rule rule;

    protected boolean readonly;

    @Override
    public NodeType getType() {
        return NodeType.SequenceConnNode;
    }


}