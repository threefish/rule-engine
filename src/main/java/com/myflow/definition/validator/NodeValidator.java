package com.myflow.definition.validator;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/30
 */
public interface NodeValidator<T> {
    /**
     * 校验
     *
     * @param t
     * @throws Exception
     */
    void check(T t) throws Exception;

}
