package cn.xjbpm.rule.engine.definition.validator;

import cn.hutool.core.util.StrUtil;
import cn.xjbpm.rule.engine.definition.model.Node;
import org.springframework.util.Assert;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/30
 */
public class BaseNodeValidator<T> implements NodeValidator<T> {


    public void check(T t) {
        Node node = ((Node) t);
        Assert.isTrue(StrUtil.isNotBlank(node.getKey()), "节点ID不能为空");
        Assert.isTrue(StrUtil.isNotBlank(node.getName()), "节点名称不能为空");
    }
}
