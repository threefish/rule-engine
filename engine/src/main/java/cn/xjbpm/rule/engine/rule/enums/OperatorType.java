/**
 * Copyright 2025 threefish.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.xjbpm.rule.engine.rule.enums;

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