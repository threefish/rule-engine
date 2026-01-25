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
package cn.xjbpm.rule.engine.aviator.function.incometax;

import cn.xjbpm.rule.engine.aviator.function.AbstractBaseFunction;
import cn.xjbpm.rule.engine.aviator.function.AviatorExtendFunction;
import com.googlecode.aviator.runtime.type.AviatorLong;
import com.googlecode.aviator.runtime.type.AviatorObject;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 个人所得税税率获取函数
 * 强化了防御性编程，根据应纳税所得额计算对应的税率百分比，严格执行非空及类型校验。
 *
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/7/1
 */
@SuppressWarnings("all")
public class GET_INCOME_TAX_RATE extends AbstractBaseFunction {

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        Object target = arg1.getValue(env);

        // 防御性编程：根据要求，参数为 null 必须抛出异常
        Assert.notNull(target, String.format("函数 %s 的参数(应纳税所得额)不能为空", getName()));

        // 校验是否为数字类型，非数字类型在税务计算中应视为非法输入并抛出异常
        if (!(target instanceof Number)) {
            throw new IllegalArgumentException(String.format("函数 %s 期待数字类型参数，实际类型为: %s",
                    getName(), target.getClass().getName()));
        }

        long value = ((Number) target).longValue();
        int rate = 3; // 默认起始税率为 3%

        // 逻辑优化：从最高阈值开始判断，确保阶梯税率匹配正确
        if (value >= 960000) {
            rate = 45;
        } else if (value >= 660000) {
            rate = 35;
        } else if (value >= 420000) {
            rate = 30;
        } else if (value >= 300000) {
            rate = 25;
        } else if (value >= 144000) {
            rate = 20;
        } else if (value >= 36000) {
            rate = 10;
        }

        return AviatorLong.valueOf(rate);
    }

    @Override
    public List<AviatorExtendFunction> docs() {
        return Collections.singletonList(
                new AviatorExtendFunction(
                        getName(),
                        String.format("%s(income)", getName()),
                        "number",
                        "获取个税税率(%)。基于应纳税所得额返回对应税率。任意参数为 null 或非数字类型将抛出异常。",
                        String.format("%s(150000)", getName()))
        );
    }
}