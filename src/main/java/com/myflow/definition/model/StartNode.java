package com.myflow.definition.model;

import lombok.Data;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/28
 */
@Data
public class StartNode extends Node {

    @Override
    protected NodeType getType() {
        return NodeType.StartNode;
    }


}
