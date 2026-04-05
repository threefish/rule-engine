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
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.stream.Stream;

/**
 * RocketMQ节点（消息生产者）
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RocketMQNode extends Node {

    /**
     * 凭据ID
     */
    private String credentialId;

    /**
     * 主题
     */
    private String topic;

    /**
     * 消息标签（Tag）
     */
    private String tag;

    /**
     * 消息键（Key）
     */
    private String messageKey;

    /**
     * 消息体
     */
    private String messageBody;

    /**
     * 发送模式
     */
    private SendMode sendMode = SendMode.SYNC;

    /**
     * 消息延迟级别（0表示不延迟）
     * 1-18分别对应：1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h
     */
    private Integer delayLevel = 0;

    /**
     * 消息属性JSON
     */
    private String otherProperties;

    @Override
    public NodeType getType() {
        return NodeType.RocketMQNode;
    }

    /**
     * 发送模式枚举
     */
    public enum SendMode {
        SYNC("sync"),
        ASYNC("async"),
        ONEWAY("oneway");

        @JsonValue
        private final String value;

        SendMode(String value) {
            this.value = value;
        }

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public static SendMode fromValue(String value) {
            return Stream.of(SendMode.values())
                    .filter(r -> r.value.equals(value))
                    .findFirst()
                    .orElse(SYNC);
        }
    }
}
