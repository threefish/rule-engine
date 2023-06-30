package com.myflow.definition.model;

import com.myflow.rule.enums.VariableType;
import lombok.Data;

import java.util.List;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/6/30
 */
@Data
public class ObjectModel {

    String key;
    String value;
    VariableType type;
    boolean required;
    boolean response;
    String label;
    List<ObjectModel> children;

}
