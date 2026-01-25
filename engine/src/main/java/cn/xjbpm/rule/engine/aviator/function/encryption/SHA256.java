/*
 * Copyright 2025 threefish.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.xjbpm.rule.engine.aviator.function.encryption;

import cn.hutool.crypto.digest.DigestUtil;
import cn.xjbpm.rule.engine.aviator.function.AbstractBaseFunction;
import cn.xjbpm.rule.engine.aviator.function.AviatorExtendFunction;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * SHA-256 加密函数
 * 强化了防御性编程，提供 64 位小写加密结果，严格执行参数非空校验。
 *
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/10/25
 */
@SuppressWarnings("all")
public class SHA256 extends AbstractBaseFunction {

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        Object valueObj = arg1.getValue(env);

        
        Assert.notNull(valueObj, String.format("函数 %s 的待加密对象(var1)不能为空", getName()));

        String message = valueObj.toString();
        // 使用 Hutool 工具类进行 SHA256 加密并转换为 64 位小写十六进制字符串
        String hex = DigestUtil.sha256Hex(message);

        return new AviatorString(hex);
    }

    @Override
    public List<AviatorExtendFunction> docs() {
        return Collections.singletonList(
                new AviatorExtendFunction(
                        getName(),
                        String.format("%s(value)", getName()),
                        "string",
                        "获取 SHA-256 加密后的 64 位小写字符串。任意参数为 null 将抛出异常。",
                        String.format("%s('hello')", getName()))
        );
    }
}