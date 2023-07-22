package cn.xjbpm.rule.engine.definition.model;

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
