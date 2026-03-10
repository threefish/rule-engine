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
package cn.xjbpm.rule.common.utils;

import cn.xjbpm.rule.engine.definition.model.nodes.RedisNode;
import cn.xjbpm.rule.engine.runtime.model.credentials.RedisCredential;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

import java.util.*;

/**
 * Redis 工具类
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Slf4j
public class RedisUtils {

    /**
     * 执行Redis操作
     */
    public static RedisResult execute(RedisCredential credential, RedisNode.OperationType operationType,
                                        String key, String field, String value, Integer ttl,
                                        Integer start, Integer stop,
                                        String luaScript, Integer keyCount, String keys, String args) {
        try (Jedis jedis = createJedis(credential)) {
            Object result = doExecute(jedis, operationType, key, field, value, ttl, start, stop, luaScript, keyCount, keys, args);
            return RedisResult.builder()
                    .success(true)
                    .data(result)
                    .operationType(operationType.name())
                    .build();
        } catch (JedisException e) {
            log.error("Redis操作失败: {}", e.getMessage(), e);
            return RedisResult.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .operationType(operationType.name())
                    .build();
        }
    }

    /**
     * 创建Jedis连接
     */
    private static Jedis createJedis(RedisCredential credential) {
        Jedis jedis = new Jedis(credential.getHost(), credential.getPort());

        String username = credential.getUsername();
        String password = credential.getPassword();

        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
            jedis.auth(username, password);
        } else if (StringUtils.isNotBlank(password)) {
            jedis.auth(password);
        }

        int database = credential.getDatabase();
        if (database > 0) {
            jedis.select(database);
        }

        return jedis;
    }

    /**
     * 执行具体操作
     */
    private static Object doExecute(Jedis jedis, RedisNode.OperationType operationType,
                                      String key, String field, String value, Integer ttl,
                                      Integer start, Integer stop,
                                      String luaScript, Integer keyCount, String keys, String args) {
        switch (operationType) {
            case GET:
                return jedis.get(key);
            case SET:
                String result = jedis.set(key, value);
                if (ttl != null && ttl > 0) {
                    jedis.expire(key, ttl);
                }
                return result;
            case DEL:
                return jedis.del(key);
            case EXISTS:
                return jedis.exists(key);
            case EXPIRE:
                return jedis.expire(key, ttl != null ? ttl : -1);
            case INCR:
                return jedis.incr(key);
            case DECR:
                return jedis.decr(key);
            case LPUSH:
                return jedis.lpush(key, value);
            case RPUSH:
                return jedis.rpush(key, value);
            case LPOP:
                return jedis.lpop(key);
            case RPOP:
                return jedis.rpop(key);
            case LLEN:
                return jedis.llen(key);
            case LRANGE:
                return jedis.lrange(key, start != null ? start : 0, stop != null ? stop : -1);
            case SADD:
                return jedis.sadd(key, value);
            case SREM:
                return jedis.srem(key, value);
            case SISMEMBER:
                return jedis.sismember(key, value);
            case SMEMBERS:
                return jedis.smembers(key);
            case SCARD:
                return jedis.scard(key);
            case HSET:
                return jedis.hset(key, field, value);
            case HGET:
                return jedis.hget(key, field);
            case HDEL:
                return jedis.hdel(key, field);
            case HEXISTS:
                return jedis.hexists(key, field);
            case HGETALL:
                return jedis.hgetAll(key);
            case EVAL:
                return executeLua(jedis, luaScript, keyCount, keys, args);
            default:
                throw new IllegalArgumentException("不支持的操作类型: " + operationType);
        }
    }

    /**
     * 执行Lua脚本
     */
    private static Object executeLua(Jedis jedis, String luaScript, Integer keyCount, String keys, String args) {
        List<String> keyList = parseJsonArray(keys);
        List<String> argList = parseJsonArray(args);
        int numKeys = keyCount != null ? keyCount : keyList.size();

        List<String> params = new ArrayList<>();
        params.addAll(keyList);
        params.addAll(argList);

        return jedis.eval(luaScript, numKeys, params.toArray(new String[0]));
    }

    /**
     * 解析JSON数组
     */
    private static List<String> parseJsonArray(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            json = json.trim();
            if (json.startsWith("[") && json.endsWith("]")) {
                json = json.substring(1, json.length() - 1);
                if (json.isEmpty()) {
                    return new ArrayList<>();
                }
                List<String> result = new ArrayList<>();
                String[] items = json.split(",");
                for (String item : items) {
                    item = item.trim();
                    if (item.startsWith("\"") && item.endsWith("\"")) {
                        result.add(item.substring(1, item.length() - 1));
                    } else {
                        result.add(item);
                    }
                }
                return result;
            }
        } catch (Exception e) {
            log.warn("解析JSON数组失败: {}", e.getMessage());
        }
        return new ArrayList<>();
    }

    /**
     * Redis操作结果
     */
    @lombok.Builder
    @lombok.Data
    public static class RedisResult {
        private boolean success;
        private Object data;
        private String errorMessage;
        private String operationType;
    }
}
