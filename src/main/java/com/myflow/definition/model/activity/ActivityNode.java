package com.myflow.definition.model.activity;

import com.myflow.definition.model.Node;
import lombok.Data;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/29
 */
@Data
public abstract class ActivityNode extends Node {


    /**
     * 异步执行
     */
    protected boolean asynchronous = false;

    /**
     * 延迟执行时间
     */
    protected Integer delayTime;

    /**
     * 不满足完成条件是否重试
     */
    protected boolean retry = false;

    /**
     * 重试次数
     */
    protected int retryTimes;

    /**
     * 忽略失败继续执行后续节点
     */
    protected boolean ignoreFailure;


}
