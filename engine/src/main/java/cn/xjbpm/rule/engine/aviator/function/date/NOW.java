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

package cn.xjbpm.rule.engine.aviator.function.date;

import cn.hutool.core.date.DateUtil;
import cn.xjbpm.rule.engine.aviator.function.AbstractBaseFunction;
import cn.xjbpm.rule.engine.aviator.function.AviatorExtendFunction;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 获取当前时间的函数
 * 强化了防御性编程，支持自定义格式，严格执行非空校验。
 *
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/10/25
 */
@SuppressWarnings("all")
public class NOW extends AbstractBaseFunction {

    /** 默认日期格式 */
    private static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Override
    public AviatorObject call(Map<String, Object> env) {
        // 无参调用时使用默认格式
        return new AviatorString(DateUtil.format(new Date(), DEFAULT_FORMAT));
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        Object formatObj = arg1.getValue(env);

        // 防御性编程：根据要求，显式传入的参数为 null 时必须抛出异常
        Assert.notNull(formatObj, String.format("函数 %s 的日期格式参数不能为空", getName()));

        String dateFormat = formatObj.toString();
        // 防呆：如果转换后是空字符串，视为非法格式
        if (dateFormat.trim().isEmpty()) {
            throw new IllegalArgumentException(String.format("函数 %s 的日期格式不能为空字符串", getName()));
        }

        try {
            // 使用 Hutool 的 DateUtil 格式化当前时间
            return new AviatorString(DateUtil.format(new Date(), dateFormat));
        } catch (Exception e) {
            // 防呆：如果日期格式定义错误（如包含非法字符），抛出详细异常
            throw new IllegalArgumentException(String.format("函数 %s 的日期格式 [%s] 无效，请检查。错误信息：%s",
                    getName(), dateFormat, e.getMessage()));
        }
    }

    @Override
    public List<AviatorExtendFunction> docs() {
        return Collections.singletonList(
                new AviatorExtendFunction(
                        getName(),
                        String.format("%s([format])", getName()),
                        "string",
                        "获取当前系统时间字符串。可选参数 format 为日期格式。如果显式传入 null 或非法格式将抛出异常。",
                        String.format("%s('yyyy-MM-dd')", getName()))
        );
    }
}