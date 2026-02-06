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

package cn.xjbpm.rule.engine.definition.model.nodes;

import cn.xjbpm.rule.engine.definition.model.enums.NodeType;
import cn.xjbpm.rule.engine.rule.enums.CombinatorType;
import cn.xjbpm.rule.engine.rule.enums.OperatorType;
import lombok.Data;
import org.springframework.http.HttpMethod;

import java.util.List;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/28
 */
@Data
public class HttpNode extends Node {


    private HttpMethod method;
    private Timeout timeout;
    private List<Header> headers;
    private List<QueryParam> queryParams;
    private Boolean enableBody;
    private String body;
    private ResponseCondition responseCondition;
    private String url;
    private String credentialId;

    @Override
    public NodeType getType() {
        return NodeType.HttpNode;
    }

    public enum Timeout {
        DEFAULT,
        FAST,
        SLOW,
        VERAY_SLOW
    }

    @Data
    public static class Header {
        private String key;
        private String value;
        private boolean enabled;
    }

    @Data
    public static class QueryParam {
        private String key;
        private String value;
        private boolean enabled;
    }

    @Data
    public static class ResponseCondition {
        private CombinatorType combination;
        private List<Condition> conditions;
    }

    @Data
    public static class Condition {
        private String name;
        private OperatorType op;
        private String value;
    }
}