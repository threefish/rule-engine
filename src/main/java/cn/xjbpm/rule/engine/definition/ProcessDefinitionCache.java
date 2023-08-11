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
     * @param id
     * @return
     */
    ProcessModel get(Long id);

    /**
     * 设置流程定义模型
     *
     * @param id
     * @param processModel
     */
    void set(Long id, ProcessModel processModel);


    /**
     * 移除
     *
     * @param id
     */
    void remove(Long id);

    /**
     * 判断是否在缓存中
     *
     * @param id
     * @return
     */

    boolean contains(Long id);

    /**
     * 清空缓存
     */
    void clear();
}
