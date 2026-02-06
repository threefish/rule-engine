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

package cn.xjbpm.rule.engine.definition.model;

import cn.xjbpm.rule.engine.definition.model.event.ExcutionListener;
import cn.xjbpm.rule.engine.definition.model.nodes.EndNode;
import cn.xjbpm.rule.engine.definition.model.nodes.Node;
import cn.xjbpm.rule.engine.definition.model.nodes.StartNode;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/28
 */
@Data
public class RuleFlowModel implements java.io.Serializable {

    /**
     * key
     */
    protected String key;
    /**
     * 名称
     */
    protected String name;
    /**
     * 描述文档
     */
    protected String description;
    /**
     * 超时秒数
     */
    protected Long timeoutSeconds;
    /**
     * 执行侦听器
     */
    protected List<ExcutionListener> executionListeners = new ArrayList<>();
    /**
     * 业务对象模型
     */
    protected List<ObjectModel> businessObjectModels = new ArrayList<>();
    /**
     * 子节点
     */
    private List<? extends Node> childNodes = new ArrayList<>();

    private StartNode startNode;

    private EndNode endNode;
    /**
     * 原始json
     */
    private String originalJson;


}