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

    private final Map<String, ProcessModel> cache = Collections.synchronizedMap(new HashMap<>());

    @Override
    public ProcessModel get(String deploymentId) {
        return cache.get(deploymentId);
    }

    @Override
    public void set(String deploymentId, ProcessModel processModel) {
        cache.put(deploymentId, processModel);
    }

    @Override
    public void remove(String deploymentId) {
        cache.remove(deploymentId);
    }

    @Override
    public boolean contains(String deploymentId) {
        return cache.containsKey(deploymentId);
    }

    @Override
    public void clear() {
        cache.clear();
    }
}
