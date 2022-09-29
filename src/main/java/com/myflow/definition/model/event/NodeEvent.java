package com.myflow.definition.model.event;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/29
 */
public enum NodeEvent {
    /**
     * 开始
     */
    START,
    /**
     * 完成
     */
    COMPLETE,
    /**
     * 开始重试
     */
    START_RETRY,

    /**
     * 跳过
     */
    SKIP,
    /**
     * 失败
     */
    FAILED
}
