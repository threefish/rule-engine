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
package cn.xjbpm.rule.engine.aviator.functions;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.xjbpm.rule.engine.aviator.annotation.FunctionDoc;
import cn.xjbpm.rule.engine.aviator.annotation.FunctionNamespace;
import org.nutz.lang.random.R;

import java.util.Date;

/**
 * 增强版 ID 工具类
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@FunctionNamespace(name = "id")
@SuppressWarnings("all")
public class IdUtils {

    @FunctionDoc(value = "id.uuid()", description = "生成标准UUID", example = "id.uuid()")
    public static String uuid() {
        return IdUtil.fastSimpleUUID();
    }

    @FunctionDoc(value = "id.shortId()", description = "生成短UUID (22位)", example = "id.shortId()")
    public static String shortId() {
        return R.UU64();
    }

    @FunctionDoc(value = "id.nextId()", description = "生成长整型雪花ID", example = "id.nextId()")
    public static long nextId() {
        return IdUtil.getSnowflakeNextId();
    }

    @FunctionDoc(value = "id.nextIdStr(prefix)", description = "生成字符串雪花ID", example = "id.nextIdStr('ORDER_')")
    public static String nextIdStr(String prefix) {
        if (StrUtil.isNotBlank(prefix)) {
            return prefix + IdUtil.getSnowflakeNextIdStr();
        }
        return IdUtil.getSnowflakeNextIdStr();
    }

    @FunctionDoc(value = "id.objectId()", description = "生成MongoDB风格的ObjectId", example = "id.objectId()")
    public static String objectId() {
        return IdUtil.objectId();
    }

    @FunctionDoc(value = "id.bizNo(prefix, length)", description = "生成业务流水号: 前缀+年月日时分秒+指定长度随机数字", example = "id.bizNo('PAY', 4) -> PAY202505201430001234")
    public static String bizNo(String prefix, int randomLength) {
        String datetime = DateUtil.format(new Date(), "yyyyMMddHHmmss");
        String random = RandomUtil.randomNumbers(randomLength);
        return StrUtil.nullToEmpty(prefix) + datetime + random;
    }

    @FunctionDoc(value = "id.nanoId(size)", description = "生成指定长度的NanoID (比UUID更现代)", example = "id.nanoId(10)")
    public static String nanoId(int size) {
        return IdUtil.nanoId(size);
    }
}