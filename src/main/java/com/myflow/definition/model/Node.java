package com.myflow.definition.model;

import com.myflow.definition.model.event.ExcutionListener;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/28 活动
 */
@Data
public abstract class Node {
    /**
     * key
     */
    protected String key;
    /**
     * 名称
     */
    protected String name;
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
    protected Object behavior;
    /**
     * 执行侦听器
     */
    protected List<ExcutionListener> executionListeners = new ArrayList<>();

    /**
     * 获取类型
     *
     * @return
     */
    protected abstract NodeType getType();

}
