package cn.xjbpm.rule.engine.runtime.behavior;

import cn.xjbpm.rule.engine.runtime.context.ExecutionScope;
import cn.xjbpm.rule.engine.runtime.entity.ExecutionEntity;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/30
 */
public interface NodeBehavior {

    /**
     * 执行方法
     */
    void execution(ExecutionEntity executionEntity);

    void execution(ExecutionEntity executionEntity, ExecutionScope executionScope);

    /**
     * 离开节点
     */
    void leave(ExecutionEntity executionEntity);

    void leave(ExecutionEntity executionEntity, ExecutionScope executionScope);

}
