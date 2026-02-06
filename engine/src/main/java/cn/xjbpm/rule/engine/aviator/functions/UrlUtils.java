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

import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * URL 工具类
 * 提供 URL 编码、解码及合法性校验。支持 Object 入参并严格执行非空校验。
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@FunctionNamespace(name = "url")
@SuppressWarnings("all")
public class UrlUtils {

    @FunctionDoc(value = "url.encode(var1)", description = "URL 编码（UTF-8）。参数为 null 将抛出异常。编码失败将抛出运行时异常。", example = "url.encode('http://www.jd.com')")
    public static String encode(Object url) {
        Assert.notNull(url, "url.encode -> 参数不能为空");
        try {
            return URLEncoder.encode(url.toString(), StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            throw new RuntimeException("URL 编码失败: " + url, e);
        }
    }

    @FunctionDoc(value = "url.decode(var1)", description = "URL 解码（UTF-8）。参数为 null 将抛出异常。解码失败将抛出运行时异常。", example = "url.decode('http%3A%2F%2Fwww.jd.com')")
    public static String decode(Object url) {
        Assert.notNull(url, "url.decode -> 参数不能为空");
        try {
            return URLDecoder.decode(url.toString(), StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            throw new RuntimeException("URL 解码失败: " + url, e);
        }
    }

    @FunctionDoc(value = "url.isValid(var1)", description = "校验 URL 是否有效（必须包含 http/https 协议）。参数为 null 将抛出异常。", example = "url.isValid('https://google.com')")
    public static boolean isValid(Object url) {
        Assert.notNull(url, "url.isValid -> 参数不能为空");
        String urlStr = url.toString();
        if (urlStr.isEmpty()) {
            return false;
        }
        try {
            URL u = new URL(urlStr);
            u.toURI();
            String protocol = u.getProtocol();
            return "http".equalsIgnoreCase(protocol) || "https".equalsIgnoreCase(protocol);
        } catch (Exception e) {
            return false;
        }
    }
}