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

package cn.xjbpm.rule.engine.aviator.function.encryption;

import cn.xjbpm.rule.engine.aviator.function.AbstractBaseFunction;
import cn.xjbpm.rule.engine.aviator.function.AviatorExtendFunction;
import com.googlecode.aviator.runtime.type.AviatorNumber;
import com.googlecode.aviator.runtime.type.AviatorObject;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 数值转换函数
 * 强化了防御性编程，支持将对象转换为数值类型，严格执行参数非空校验。
 *
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/10/25
 */
@SuppressWarnings("all")
public class VALUE extends AbstractBaseFunction {

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        Object valueObj = arg1.getValue(env);

        // 防御性编程：根据要求，参数为 null 必须抛出异常
        Assert.notNull(valueObj, String.format("函数 %s 的参数不能为空", getName()));

        try {
            // 尝试转换为 BigDecimal，支持数字、科学计数法及数字字符串
            BigDecimal numericValue;
            if (valueObj instanceof BigDecimal) {
                numericValue = (BigDecimal) valueObj;
            } else {
                numericValue = new BigDecimal(valueObj.toString().trim());
            }
            return AviatorNumber.valueOf(numericValue);
        } catch (Exception e) {
            // 防呆：如果转换失败（非数字格式），抛出参数异常
            throw new IllegalArgumentException(String.format("函数 %s 无法将输入值 [%s] 转换为数值。错误信息：%s",
                    getName(), valueObj, e.getMessage()));
        }
    }

    @Override
    public List<AviatorExtendFunction> docs() {
        return Collections.singletonList(
                new AviatorExtendFunction(
                        getName(),
                        String.format("%s(value)", getName()),
                        "number",
                        "将输入对象（字符串或数字）转换为数值类型。任意参数为 null 或转换失败将抛出异常。",
                        String.format("%s('123.45')", getName()))
        );
    }
}