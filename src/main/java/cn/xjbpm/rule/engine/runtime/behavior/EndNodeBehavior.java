package cn.xjbpm.rule.engine.runtime.behavior;

import cn.xjbpm.rule.engine.definition.model.EndNode;
import cn.xjbpm.rule.engine.runtime.entity.ExecutionEntity;
import lombok.extern.slf4j.Slf4j;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/30
 */
@Slf4j
public class EndNodeBehavior implements NodeBehavior {

    private final EndNode node;

    public EndNodeBehavior(EndNode node) {
        this.node = node;
    }


    @Override
    public void execution(ExecutionEntity executionEntity) {
        log.info("流程结束 {}", node.getKey());
    }

    @Override
    public void leave(ExecutionEntity executionEntity) {

    }
}
