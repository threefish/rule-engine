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

/**
 * 钉钉消息节点
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Data
public class DingTalkNode extends Node {

    /**
     * 自定义机器人Webhook地址
     */
    private String webhookUrl;

    /**
     * 消息内容
     */
    private String messageContent;

    /**
     * 签名密钥（自定义机器人加签时使用）
     */
    private String secret;

    /**
     * @用户ID（多个用逗号分隔）
     */
    private String at;

    /**
     * 是否@所有人
     */
    private boolean atAll = false;

    @Override
    public NodeType getType() {
        return NodeType.DingTalkNode;
    }

}
