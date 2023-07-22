package cn.xjbpm.rule.engine.definition;

import cn.xjbpm.rule.engine.definition.model.ProcessModel;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/7/22
 */
public interface ProcessDefinitionCache {
    /**
     * 获取流程定义模型
     *
     * @param deploymentId
     * @return
     */
    ProcessModel get(String deploymentId);

    /**
     * 设置流程定义模型
     *
     * @param deploymentId
     * @param processModel
     */
    void set(String deploymentId, ProcessModel processModel);


    /**
     * 移除
     *
     * @param deploymentId
     */
    void remove(String deploymentId);

    /**
     * 判断是否在缓存中
     *
     * @param deploymentId
     * @return
     */

    boolean contains(String deploymentId);

    /**
     * 清空缓存
     */
    void clear();
}
