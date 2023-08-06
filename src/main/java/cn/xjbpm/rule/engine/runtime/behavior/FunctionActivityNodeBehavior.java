package cn.xjbpm.rule.engine.runtime.behavior;

import cn.xjbpm.rule.engine.definition.model.Node;
import cn.xjbpm.rule.engine.definition.model.activity.FunctionActivityNode;
import cn.xjbpm.rule.engine.runtime.entity.ExecutionEntity;
import lombok.extern.slf4j.Slf4j;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/30
 */
@Slf4j
public class FunctionActivityNodeBehavior extends BaseNodeBehavior {

    private final FunctionActivityNode node;

    public FunctionActivityNodeBehavior(FunctionActivityNode node) {
        this.node = node;
    }

    @Override
    public void unableToComplete(ExecutionEntity executionEntity) {
        log.info("节点[{}]不满足完成条件，暂挂在此节点", node.getKey());
    }

    @Override
    public void doExecution(ExecutionEntity executionEntity) {
        log.info("开始执行函数:{}", node.getCode());
        log.info("函数执行完成");
    }

    @Override
    public Node getCurrentNode() {
        return node;
    }


}
