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
 * Redis节点
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Data
public class RedisNode extends Node {

    private String credentialId;
    private OperationType operationType = OperationType.GET;
    private String key;
    private String field;
    private String value;
    private Integer ttl = -1;
    private Integer start = 0;
    private Integer stop = -1;
    private String luaScript;
    private Integer keyCount;
    private String keys;
    private String args;

    @Override
    public NodeType getType() {
        return NodeType.RedisNode;
    }

    public enum OperationType {
        GET,
        SET,
        DEL,
        EXISTS,
        EXPIRE,
        INCR,
        DECR,
        LPUSH,
        RPUSH,
        LPOP,
        RPOP,
        LLEN,
        LRANGE,
        SADD,
        SREM,
        SISMEMBER,
        SMEMBERS,
        SCARD,
        HSET,
        HGET,
        HDEL,
        HEXISTS,
        HGETALL,
        EVAL
    }
}
