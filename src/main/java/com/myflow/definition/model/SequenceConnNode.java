package com.myflow.definition.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonIgnore
    protected Node sourceNode;

    /**
     * 目标节点
     */
    @JsonIgnore
    protected Node targetNode;


    @Override
    protected NodeType getType() {
        return NodeType.SequenceConnNode;
    }



}
