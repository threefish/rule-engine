package com.myflow.model;

import lombok.Data;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/28
 */
@Data
public abstract class Node {
    /**
     * key
     */
    protected String key;
    /**
     * 名称
     */
    protected String name;
    /**
     * 描述
     */
    protected String descript;


}
