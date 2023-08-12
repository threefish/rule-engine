package cn.xjbpm.rule.engine.adapter.persistence;

import cn.hutool.core.io.IoUtil;
import cn.xjbpm.rule.engine.adapter.persistence.po.ProcessDefinitionEntity;
import org.springframework.stereotype.Repository;

import java.io.InputStream;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/7/22
 */
@Repository
public class ProcessDefinitionRepository {
    /**
     * 获取流程定义主版本信息
     *
     * @param key
     * @return
     */
    public ProcessDefinitionEntity getMainProcessDefinition(String key, Integer version) {
        InputStream resourceAsStream = ProcessDefinitionRepository.class.getResourceAsStream("/process/个人所得税计算.json");
        ProcessDefinitionEntity processDefinition = new ProcessDefinitionEntity();
        processDefinition.setDefinitionKey(key);
        processDefinition.setDefinitionName(key);
        processDefinition.setId(1L);
        processDefinition.setDefinitionContent(IoUtil.readUtf8(resourceAsStream));
        return processDefinition;
    }

}
