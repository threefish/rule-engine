package cn.xjbpm.rule.engine.common.utils;

import cn.hutool.core.util.IdUtil;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/8/12
 */
public class IdGenerUtils {
    /**
     * 全局
     *
     * @return
     */
    public static long nextGlobalId() {
        return IdUtil.getSnowflakeNextId();
    }
}
