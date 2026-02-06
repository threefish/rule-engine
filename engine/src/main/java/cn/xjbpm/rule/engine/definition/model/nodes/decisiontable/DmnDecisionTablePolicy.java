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
package cn.xjbpm.rule.engine.definition.model.nodes.decisiontable;

import cn.xjbpm.rule.engine.rule.enums.VariableType;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2026/2/3
 */
@Data
public class DmnDecisionTablePolicy {

    private HitPolicy hitPolicy;
    private List<Row> rules;
    private List<Column> inputColumns;
    private List<Column> outputColumns;

    public void setRules(List<Row> rules) {
        this.rules = rules;
    }

    public enum Type {
        input, output
    }

    public enum HitPolicy {

        UNIQUE, FIRST, PRIORITY, ANY, COLLECT, COLLECT_SUM, COLLECT_MIN, COLLECT_MAX, COLLECT_COUNT, RULE_ORDER, OUTPUT_ORDER
    }

    @Data
    public static class Row {
        private final Map<String, String> dynamicFields = new HashMap<>();
        private String id;
        private String description;

        /**
         * col field ->>  缓存表达式
         */
        private HashMap<String, String> cacheExpression = new HashMap<>();

        @JsonAnySetter
        public void setDynamicField(String key, String value) {
            this.dynamicFields.put(key, value);
        }

        @JsonAnyGetter
        public Map<String, String> getDynamicFields() {
            return dynamicFields;
        }

        public String getDynamicValue(String key) {
            return this.dynamicFields.get(key);
        }
    }

    @Data
    public static class Column {
        private String id;
        private String name;
        private String field;
        private String assignmentField;
        private Type type;
        private String width;
        private VariableType varType;
        /**
         * 允许值列表。
         * 用于 PRIORITY 和 OUTPUT_ORDER 策略的排序依据。
         * 例如：["High", "Medium", "Low"]，则 High 的优先级高于 Low。
         */
        private List<String> allowedValues;

    }
}