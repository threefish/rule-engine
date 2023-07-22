package cn.xjbpm.rule.engine.runtime;

import cn.xjbpm.rule.engine.runtime.context.ProcessRuntimeContext;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/30
 */
public interface ProcessRunService {

    /**
     * 开始流程
     *
     * @param processRuntimeContext
     * @return processId 流程ID
     */
    Long start(ProcessRuntimeContext processRuntimeContext);

    /**
     * 结束流程
     *
     * @param processId
     * @param reason
     * @return
     */
    boolean stop(Long processId, String reason);

}
