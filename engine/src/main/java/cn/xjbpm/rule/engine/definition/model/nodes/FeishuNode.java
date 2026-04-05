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
 * 飞书消息节点
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FeishuNode extends Node {

    /**
     * 飞书应用凭据ID
     */
    private String credentialId;

    /**
     * 操作类型
     */
    private OperationType operationType = OperationType.SEND;

    /**
     * 消息接收者ID类型（发送消息时使用）
     */
    private ReceiverIdType receiverIdType = ReceiverIdType.OPEN_ID;

    /**
     * 消息接收者ID（发送消息时使用，多个用逗号分隔）
     */
    private String receiverId;

    /**
     * 待回复的消息ID（回复消息时使用）
     */
    private String messageId;

    /**
     * 消息类型
     */
    private MsgType msgType = MsgType.TEXT;

    /**
     * 消息内容
     */
    private String messageContent;

    /**
     * 是否回复到话题（回复消息时使用）
     */
    private boolean replyInThread = false;

    @Override
    public NodeType getType() {
        return NodeType.FeishuNode;
    }

    /**
     * 操作类型枚举
     */
    public enum OperationType {
        /**
         * 发送消息
         */
        SEND("SEND"),
        /**
         * 回复消息
         */
        REPLY("REPLY");

        @JsonValue
        private final String value;

        OperationType(String value) {
            this.value = value;
        }

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public static OperationType fromValue(String value) {
            return Stream.of(OperationType.values())
                    .filter(r -> r.value.equals(value))
                    .findFirst()
                    .orElse(SEND);
        }
    }

    /**
     * 消息接收者ID类型枚举
     */
    public enum ReceiverIdType {
        /**
         * 用户open_id
         */
        OPEN_ID("open_id"),
        /**
         * 用户user_id
         */
        USER_ID("user_id"),
        /**
         * 用户union_id
         */
        UNION_ID("union_id"),
        /**
         * 用户邮箱
         */
        EMAIL("email"),
        /**
         * 群聊chat_id
         */
        CHAT_ID("chat_id");

        @JsonValue
        private final String value;

        ReceiverIdType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public static ReceiverIdType fromValue(String value) {
            return Stream.of(ReceiverIdType.values())
                    .filter(r -> r.value.equals(value))
                    .findFirst()
                    .orElse(OPEN_ID);
        }
    }

    /**
     * 消息类型枚举
     */
    public enum MsgType {
        /**
         * 文本消息
         */
        TEXT("text"),
        /**
         * 富文本消息
         */
        POST("post");

        @JsonValue
        private final String value;

        MsgType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public static MsgType fromValue(String value) {
            return Stream.of(MsgType.values())
                    .filter(r -> r.value.equals(value))
                    .findFirst()
                    .orElse(TEXT);
        }
    }
}
