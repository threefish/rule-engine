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

import cn.xjbpm.rule.engine.rule.translate.*;
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


    NUMBER("number", new NumberTranslate()),
    DATE_TIME("dateTime", new DateTimeTranslate()),
    DATE("date", new DateTranslate()),
    LIST("list", new ListTranslate()),
    STRING("string", new StringTranslate()),
    BOOLEAN("boolean", new BooleanTranslate()),
    OBJECT("object", new ObjectTranslate()),
    ;

    @JsonValue
    String value;

    AbstractTranslate translate;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static VariableType fromValue(String value) {
        return Stream.of(VariableType.values())
                .filter(r -> r.getValue().equals(value))
                .findFirst()
                .orElse(null);
    }
}