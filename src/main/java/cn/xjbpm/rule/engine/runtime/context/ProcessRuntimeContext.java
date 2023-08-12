package cn.xjbpm.rule.engine.runtime.context;

import cn.xjbpm.rule.engine.adapter.persistence.po.ProcessDefinitionEntity;
import lombok.Data;

import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/8/12
 */
@Data
public class ProcessRuntimeContext {

    Long processIntanceId;

    ProcessDefinitionEntity processDefinition;

    Map<String, Object> variable;
}
