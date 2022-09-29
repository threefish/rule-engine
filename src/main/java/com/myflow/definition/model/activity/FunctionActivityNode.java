package com.myflow.definition.model.activity;

import com.myflow.definition.model.NodeType;
import lombok.Data;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/28
 */
@Data
public class FunctionActivityNode extends ActivityNode {

    /**
     * 函数编码
     */
    String code;


    @Override
    protected NodeType getType() {
        return NodeType.FunctionActivityNode;
    }
}
