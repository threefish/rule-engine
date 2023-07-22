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
