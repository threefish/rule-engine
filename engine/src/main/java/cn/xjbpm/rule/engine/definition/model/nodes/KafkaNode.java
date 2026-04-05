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

/**
 * Kafka节点（消息生产者）
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class KafkaNode extends Node {

    private String credentialId;
    private String topic;

    private String messageKey;
    private String messageValue;
    private AckMode ackMode = AckMode.ALL;
    private Integer partition;
    private String headers;

    @Override
    public NodeType getType() {
        return NodeType.KafkaNode;
    }

    public enum AckMode {
        NONE,
        LEADER,
        ALL
    }
}
