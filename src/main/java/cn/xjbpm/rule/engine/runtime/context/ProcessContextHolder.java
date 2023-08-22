package cn.xjbpm.rule.engine.runtime.context;

import cn.xjbpm.rule.engine.common.enums.ProcessStatusEnum;
import cn.xjbpm.rule.engine.definition.model.ProcessModel;

import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/8/12
 */
public class ProcessContextHolder {

    private static final ThreadLocal<ProcessRuntimeContext> THREAD_LOCAL = new ThreadLocal<>();

    public static ProcessRuntimeContext createContext(Long processIntanceId, String environment, ProcessModel processModel, Map<String, Object> variable) {
        ProcessRuntimeContext processRuntimeContext = new ProcessRuntimeContext();
        processRuntimeContext.setProcessInstanceId(processIntanceId);
        processRuntimeContext.setProcessStatus(ProcessStatusEnum.UNDERWAY);
        processRuntimeContext.setVariable(variable);
        processRuntimeContext.setProcessModel(processModel);
        processRuntimeContext.setEnvironment(environment);
        THREAD_LOCAL.set(processRuntimeContext);
        return processRuntimeContext;
    }

    public static ProcessRuntimeContext getContext() {
        return THREAD_LOCAL.get();
    }


    public static void destroy() {
        THREAD_LOCAL.remove();
    }
}
