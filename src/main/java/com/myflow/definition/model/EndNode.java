package com.myflow.definition.model;

import lombok.Data;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/28
 */
@Data
public class EndNode extends Node {

    @Override
    public NodeType getType() {
        return NodeType.EndNode;
    }

}
