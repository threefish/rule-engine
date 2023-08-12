package cn.xjbpm.rule.engine.runtime.context;

import cn.xjbpm.rule.engine.adapter.persistence.po.ProcessDefinitionEntity;

import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/8/12
 */
public class ProcessContextHolder {

    private static final ThreadLocal<ProcessRuntimeContext> THREAD_LOCAL = new ThreadLocal<>();

    public static ProcessRuntimeContext createContext(Long processIntanceId, ProcessDefinitionEntity processDefinition, Map<String, Object> variable) {
        ProcessRuntimeContext processRuntimeContext = new ProcessRuntimeContext();
        processRuntimeContext.setProcessIntanceId(processIntanceId);
        processRuntimeContext.setProcessDefinition(processDefinition);
        processRuntimeContext.setVariable(variable);
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
