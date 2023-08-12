package cn.xjbpm.rule.engine.adapter;

import cn.xjbpm.rule.engine.common.utils.BeanContextUtil;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/8/12
 */
@SuppressWarnings("all")
public abstract class AdapterContextHolder {

    public static final NodeExecutionAdapter nodeExecutionAdapter = BeanContextUtil.getBean(NodeExecutionAdapter.class);
}
