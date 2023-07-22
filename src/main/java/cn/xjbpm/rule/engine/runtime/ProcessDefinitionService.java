package cn.xjbpm.rule.engine.runtime;

import cn.xjbpm.rule.engine.definition.ProcessDefinitionCache;
import cn.xjbpm.rule.engine.definition.model.ProcessModel;
import cn.xjbpm.rule.engine.definition.parse.ProcessModelParse;
import cn.xjbpm.rule.engine.persistence.ProcessDefinitionRepository;
import cn.xjbpm.rule.engine.persistence.po.ProcessDefinition;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/7/22
 */
@Service
@AllArgsConstructor
public class ProcessDefinitionService {

    private static final ProcessModelParse PROCESS_MODEL_JSON_CONVERTER = new ProcessModelParse();

    private final ProcessDefinitionCache processDefinitionCache;

    private final ProcessDefinitionRepository processDefinitionRepository;


    /**
     * @param key
     * @param version 版本为空取最新
     * @return
     */
    public ProcessDefinition getProcessDefinition(String key, Integer version) {
        return processDefinitionRepository.getMainProcessDefinition(key, version);
    }

    /**
     * @param key
     * @param version 版本为空取最新
     * @return
     */
    public ProcessModel getProcessModel(String key, Integer version) {
        String deploymentId = String.format("%s:%s", key, Objects.isNull(version) ? "MAX" : version);
        if (processDefinitionCache.contains(deploymentId)) {
            return processDefinitionCache.get(deploymentId);
        }
        ProcessDefinition processDefinition = getProcessDefinition(key, version);
        ProcessModel processModel = PROCESS_MODEL_JSON_CONVERTER.convertToModel(processDefinition.getDefinitionContent());
        processDefinitionCache.set(deploymentId, processModel);
        return processModel;
    }
}
