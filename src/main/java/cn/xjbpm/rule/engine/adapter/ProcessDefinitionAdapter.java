package cn.xjbpm.rule.engine.adapter;

import cn.xjbpm.rule.engine.definition.ProcessDefinitionCache;
import cn.xjbpm.rule.engine.definition.model.ProcessModel;
import cn.xjbpm.rule.engine.definition.parse.ProcessModelParse;
import cn.xjbpm.rule.engine.adapter.persistence.ProcessDefinitionRepository;
import cn.xjbpm.rule.engine.adapter.persistence.po.ProcessDefinitionEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/7/22
 */
@Service
@AllArgsConstructor
public class ProcessDefinitionAdapter {

    private static final ProcessModelParse PROCESS_MODEL_JSON_CONVERTER = new ProcessModelParse();

    private final ProcessDefinitionCache processDefinitionCache;

    private final ProcessDefinitionRepository processDefinitionRepository;


    /**
     * @param key
     * @param version 版本为空取最新
     * @return
     */
    public ProcessDefinitionEntity getProcessDefinition(String key, Integer version) {
        return processDefinitionRepository.getMainProcessDefinition(key, version);
    }

    /**
     * @param processDefinition
     * @return
     */
    public ProcessModel getProcessModel(ProcessDefinitionEntity processDefinition) {
        ProcessModel processModel = PROCESS_MODEL_JSON_CONVERTER.convertToModel(processDefinition.getDefinitionContent());
        processDefinitionCache.set(processDefinition.getId(), processModel);
        return processModel;
    }
}
