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
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/28
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DBNode extends Node {

    private String credentialId;
    private String sql;
    private int maxResultCount;
    private ExecuteType executeType;

    private List<Param> params;


    @Override
    public NodeType getType() {
        return NodeType.DBNode;
    }

    public static enum ExecuteType {

        SELECT_ONE,
        SELECT_LIST,

        UPDATE,

    }

    @Data
    public static class Param {
        private String field;
        private String expression;
        private boolean nullable;
    }

}