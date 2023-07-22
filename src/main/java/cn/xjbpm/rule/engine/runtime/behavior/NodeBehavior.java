package cn.xjbpm.rule.engine.runtime.behavior;

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

    /**
     * 离开节点
     */
    void leave(ExecutionEntity executionEntity);

}
