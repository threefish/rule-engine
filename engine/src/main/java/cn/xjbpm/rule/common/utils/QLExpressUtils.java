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

import com.alibaba.qlexpress4.Express4Runner;
import com.alibaba.qlexpress4.InitOptions;
import com.alibaba.qlexpress4.QLOptions;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * QLExpress 脚本工具类
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Slf4j
public class QLExpressUtils {

    private static final Express4Runner EXPRESS_RUNNER = new Express4Runner(InitOptions.DEFAULT_OPTIONS);


    /**
     * 执行 QLExpress 脚本
     *
     * @param scriptContent 脚本内容
     * @param variables     变量上下文
     * @return 执行结果
     */
    public static Object execute(String scriptContent, Map<String, Object> variables) {
        if (scriptContent == null || scriptContent.trim().isEmpty()) {
            return null;
        }
        try {
            Object result = EXPRESS_RUNNER.execute(scriptContent, variables, QLOptions.DEFAULT_OPTIONS).getResult();
            return result;
        } catch (Exception e) {
            throw new RuntimeException("QLExpress 脚本执行失败", e);
        }
    }
}
