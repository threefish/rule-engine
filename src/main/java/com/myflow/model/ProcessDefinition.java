package com.myflow.model;

import lombok.Data;

import java.util.List;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/28
 */
@Data
public class ProcessDefinition {

    /**
     * 子节点
     */
    private List<? extends Node> childNode;



}
