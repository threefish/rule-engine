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
public enum OperatorType {
    EQ("="),
    NOT_EQ("!="),
    LT("<"),
    GT(">"),
    LTE("<="),
    GTE(">="),

    CONTAINS("contains"),
    NOT_CONTAINS("notContains"),


    LIKE("like"),
    NOT_LIKE("notLike"),
    BEGIN_WITH("beginWith"),
    END_WITH("endWith"),
    DOES_NOT_BEGIN_WITH("doesNotBeginWith"),
    DOES_NOT_END_WITH("doesNotEndWith"),


    IS_NULL("isNull"),
    IS_NOT_NULL("isNotNull"),

    IN_COLLECTION("inCollection"),
    NOT_IN_COLLECTION("notInCollection"),

    BETWEEN("between"),
    NOT_BETWEEN("notBetween"),
    IS_EMPTY("isEmpty"),
    IS_NOT_EMPTY("isNotEmpty"),
    
    
    ;


    @JsonValue
    String value;


    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static OperatorType fromValue(String value) {
        return Stream.of(OperatorType.values())
                .filter(r -> r.getValue().equals(value))
                .findFirst()
                .orElse(null);
    }
}
