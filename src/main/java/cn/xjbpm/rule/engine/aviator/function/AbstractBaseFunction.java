package cn.xjbpm.rule.engine.aviator.function;

import com.googlecode.aviator.runtime.function.AbstractFunction;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/7/5
 */
public abstract class AbstractBaseFunction extends AbstractFunction {

    private final String FUNCTION_NAME = getClass().getSimpleName();

    @Override
    public String getName() {
        return FUNCTION_NAME;
    }
}
