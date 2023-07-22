package cn.xjbpm.rule.engine.runtime.context;

import cn.xjbpm.rule.engine.definition.model.ProcessModel;
import lombok.Data;

import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/30
 */
@Data
public class ProcessRuntimeContext {
    /**
     * 流程模型
     */
    private ProcessModel processModel;
    /**
     * 流程变量
     */
    private Map<String, Object> variable;

    public ProcessRuntimeContext(ProcessModel processModel, Map<String, Object> variable) {
        this.processModel = processModel;
        this.variable = variable;
    }
}
