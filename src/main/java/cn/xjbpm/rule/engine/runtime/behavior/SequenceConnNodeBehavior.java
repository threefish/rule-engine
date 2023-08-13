package cn.xjbpm.rule.engine.runtime.behavior;

import cn.hutool.core.util.StrUtil;
import cn.xjbpm.rule.engine.definition.model.Node;
import cn.xjbpm.rule.engine.definition.model.SequenceConnNode;
import cn.xjbpm.rule.engine.runtime.context.ProcessContextHolder;
import cn.xjbpm.rule.engine.runtime.context.ProcessRuntimeContext;
import cn.xjbpm.rule.engine.runtime.entity.ExecutionEntity;
import cn.xjbpm.rule.engine.runtime.util.ConditionUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/30
 */
@Slf4j
public class SequenceConnNodeBehavior extends BaseNodeBehavior {

    private final SequenceConnNode node;

    public SequenceConnNodeBehavior(SequenceConnNode node) {
        this.node = node;
    }


    @Override
    public void leave(ExecutionEntity executionEntity) {
        log.info("离开[{}]节点", node.getKey());
        Node targetNode = node.getTargetNode();
        targetNode.getBehavior().execution(executionEntity);
    }

    @Override
    public void unableToComplete(ExecutionEntity executionEntity) {

    }

    @Override
    public void doExecution(ExecutionEntity executionEntity) {
        log.info("[{}]执行处理逻辑", node.getKey());
    }

    @Override
    public Node getCurrentNode() {
        return node;
    }


}
