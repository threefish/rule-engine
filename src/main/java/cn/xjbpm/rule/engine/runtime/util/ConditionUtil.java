package cn.xjbpm.rule.engine.runtime.util;

import cn.hutool.core.util.StrUtil;
import cn.xjbpm.rule.engine.runtime.entity.ExecutionEntity;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/30
 * 条件判断工具类
 */
public class ConditionUtil {
    /**
     * 决定表达式
     *
     * @param expression
     * @param executionEntity
     * @return
     */
    public static boolean resolve(String expression, ExecutionEntity executionEntity) {
        if (StrUtil.isBlank(expression)) {
            return true;
        }
        //todo 表达式解决
        return true;
    }
}
