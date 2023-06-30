package com.myflow.rule.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.stream.Stream;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/6/30
 */
@AllArgsConstructor
@Getter
public enum VariableType {


    NUMBER("number"),
    DATE_TIME("dateTime"),
    DATE("date"),
    LIST("list"),
    STRING("string"),
    BOOLEAN("boolean"),
    OBJECT("object"),
    ;

    @JsonValue
    String value;


    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static VariableType fromValue(String value) {
        return Stream.of(VariableType.values())
                .filter(r -> r.getValue().equals(value))
                .findFirst()
                .orElse(null);
    }
}
