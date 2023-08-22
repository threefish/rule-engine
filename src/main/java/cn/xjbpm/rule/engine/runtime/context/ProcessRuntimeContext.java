package cn.xjbpm.rule.engine.runtime.context;

import cn.xjbpm.rule.engine.common.enums.ProcessStatusEnum;
import cn.xjbpm.rule.engine.definition.model.ProcessModel;
import lombok.Data;

import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/8/12
 */
@Data
public class ProcessRuntimeContext {

    private Long processInstanceId;


    private ProcessStatusEnum processStatus;

    private Map<String, Object> variable;

    private ProcessModel processModel;

    /**
     * 环境
     */
    private String environment;
}
