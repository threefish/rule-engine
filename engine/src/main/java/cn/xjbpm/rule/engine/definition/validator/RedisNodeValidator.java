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

package cn.xjbpm.rule.engine.definition.validator;

import cn.xjbpm.rule.common.utils.StringUtils;
import cn.xjbpm.rule.engine.definition.model.nodes.RedisNode;
import org.springframework.util.Assert;

/**
 * @author 黄川 huchuc@vip.qq.com
 */
public class RedisNodeValidator extends BaseNodeValidator<RedisNode> {

    @Override
    public void check(RedisNode node) {
        super.check(node);
        Assert.isTrue(StringUtils.isNotBlank(node.getCredentialId()), "Redis凭据不能为空");
        Assert.notNull(node.getOperationType(), "操作类型不能为空");

        if (node.getOperationType() != RedisNode.OperationType.EVAL) {
            Assert.isTrue(StringUtils.isNotBlank(node.getKey()), "键名不能为空");
        }

        if (node.getOperationType() == RedisNode.OperationType.EVAL) {
            Assert.isTrue(StringUtils.isNotBlank(node.getLuaScript()), "Lua脚本不能为空");
        }

        if (needsValue(node.getOperationType()) && StringUtils.isBlank(node.getValue())) {
            Assert.isTrue(false, "该操作需要提供值参数");
        }

        if (needsField(node.getOperationType()) && StringUtils.isBlank(node.getField())) {
            Assert.isTrue(false, "该操作需要提供字段参数");
        }
    }

    private boolean needsValue(RedisNode.OperationType type) {
        return type == RedisNode.OperationType.SET ||
                type == RedisNode.OperationType.LPUSH ||
                type == RedisNode.OperationType.RPUSH ||
                type == RedisNode.OperationType.SADD ||
                type == RedisNode.OperationType.SREM ||
                type == RedisNode.OperationType.HSET;
    }

    private boolean needsField(RedisNode.OperationType type) {
        return type == RedisNode.OperationType.HSET ||
                type == RedisNode.OperationType.HGET ||
                type == RedisNode.OperationType.HDEL ||
                type == RedisNode.OperationType.HEXISTS;
    }
}
