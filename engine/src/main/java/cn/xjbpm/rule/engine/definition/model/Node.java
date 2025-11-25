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
package cn.xjbpm.rule.engine.definition.model;

import cn.xjbpm.rule.engine.definition.model.event.ExcutionListener;
import cn.xjbpm.rule.engine.runtime.behavior.NodeBehavior;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/28 活动
 */
@Data
public abstract class Node implements java.io.Serializable {
    /**
     * key
     */
    protected String id;
    /**
     * 名称
     */
    protected String name;
    /**
     * 标签
     */
    protected String tag;
    /**
     * 类型
     */
    protected NodeType type;
    /**
     * 描述文档
     */
    protected String documentation;

    /**
     * 顺序
     */
    protected int order;

    /**
     * 完成条件
     */
    protected String completionExpression;

    /**
     * 跳过条件
     */
    protected String skipExpression;

    /**
     * 最大重试次数
     */
    protected Integer maxRetries;
    /**
     * 重试间隔（毫秒）
     **/
    protected Integer retryDelay;

    /**
     * 传入节点
     */
    protected List<SequenceConnNode> incomingNodes = new ArrayList<>();
    /**
     * 传出节点
     */
    protected List<SequenceConnNode> outgoingNodes = new ArrayList<>();
    /**
     * 行为处理方法
     */
    protected NodeBehavior behavior;
    /**
     * 执行侦听器
     */
    protected List<ExcutionListener> executionListeners = new ArrayList<>();

    /**
     * 原始json 通过json转为model时存储，转换为json信息时还原
     */
    protected String originalJson;

    /**
     * 获取类型
     *
     * @return
     */
    public abstract NodeType getType();


}