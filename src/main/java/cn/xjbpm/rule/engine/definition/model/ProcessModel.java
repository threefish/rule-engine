package cn.xjbpm.rule.engine.definition.model;

import cn.xjbpm.rule.engine.definition.model.event.ExcutionListener;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/28
 */
@Data
public class ProcessModel {

    /**
     * key
     */
    protected String key;
    /**
     * 名称
     */
    protected String name;
    /**
     * 描述文档
     */
    protected String documentation;
    /**
     * 执行侦听器
     */
    protected List<ExcutionListener> executionListeners = new ArrayList<>();
    /**
     * 业务对象模型
     */
    protected List<ObjectModel> businessObjectModels = new ArrayList<>();
    /**
     * 子节点
     */
    private List<? extends Node> childNodes = new ArrayList<>();

    private StartNode startNode;

    private EndNode endNode;


}
