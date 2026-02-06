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

import cn.xjbpm.rule.engine.aviator.annotation.FunctionDoc;
import cn.xjbpm.rule.engine.aviator.annotation.FunctionNamespace;
import org.springframework.util.Assert;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Base64 工具类
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@FunctionNamespace(name = "base64")
@SuppressWarnings("all")
public class Base64Utils {

    @FunctionDoc(value = "base64.encode(var1)", description = "Base64 编码（使用 UTF-8）。参数为 null 将抛出异常。", example = "base64.encode('hello')")
    public static String encode(Object text) {
        Assert.notNull(text, "base64.encode -> 参数不能为空");
        byte[] bytes = text.toString().getBytes(StandardCharsets.UTF_8);
        return Base64.getEncoder().encodeToString(bytes);
    }

    @FunctionDoc(value = "base64.decode(var1)", description = "Base64 解码（使用 UTF-8）。参数为 null 将抛出异常。解码失败将抛出运行时异常。", example = "base64.decode('aGVsbG8=')")
    public static String decode(Object text) {
        Assert.notNull(text, "base64.decode -> 参数不能为空");
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(text.toString());
            return new String(decodedBytes, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Base64 解码失败，非法的 Base64 格式: " + text, e);
        }
    }
}