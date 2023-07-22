package cn.xjbpm.rule.engine.definition.validator;

import cn.hutool.core.util.StrUtil;
import cn.xjbpm.rule.engine.definition.model.activity.FunctionActivityNode;
import org.springframework.util.Assert;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/30
 */
public class FunctionActivityNodeValidator extends BaseNodeValidator<FunctionActivityNode> {

    @Override
    public void check(FunctionActivityNode functionActivityNode) {
        super.check(functionActivityNode);
        Assert.isTrue(StrUtil.isNotBlank(functionActivityNode.getCode()), "函数编码 不能为空");
    }
}
