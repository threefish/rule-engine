package cn.xjbpm.rule.engine.definition;

import cn.xjbpm.rule.engine.definition.model.ProcessModel;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/7/22
 */
public class DefaultProcessDefinitionCache implements ProcessDefinitionCache {

    private final Map<Long, ProcessModel> cache = Collections.synchronizedMap(new HashMap<>());

    @Override
    public ProcessModel get(Long id) {
        return cache.get(id);
    }

    @Override
    public void set(Long deploymentId, ProcessModel processModel) {
        cache.put(deploymentId, processModel);
    }

    @Override
    public void remove(Long deploymentId) {
        cache.remove(deploymentId);
    }

    @Override
    public boolean contains(Long deploymentId) {
        return cache.containsKey(deploymentId);
    }

    @Override
    public void clear() {
        cache.clear();
    }
}
