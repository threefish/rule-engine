/**
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
package cn.xjbpm.rule.common.utils.groovy;

import groovy.transform.ThreadInterrupt;
import groovy.transform.TimedInterrupt;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer;

import static java.util.Collections.singletonMap;

/**
 * @author 黄川 huchuc@vip.qq.com
 */
@Slf4j
public class CompilerConfigurationFactory {

    public static CompilerConfiguration create() {
        CompilerConfiguration configuration = new CompilerConfiguration();
        configuration.setSourceEncoding(CompilerConfiguration.DEFAULT_SOURCE_ENCODING);
        // 设置所有脚本的基类
        configuration.setScriptBaseClass(BaseGroovyScript.class.getName());
        configuration.addCompilationCustomizers(
                // 默认脚本最大执行时间（300秒），超过会抛出 TimeoutException 此注释不会生成监视线程。相反，它的工作方式与在代码中的适当位置放置检查类似。
                // 这意味着，如果您的线程被 I/O 阻止，则不会中断。@ThreadInterrupt
                new ASTTransformationCustomizer(singletonMap("value", 300), TimedInterrupt.class),
                // 搭配这个和线程池执行脚本可以进一步防止线程占用
                new ASTTransformationCustomizer(ThreadInterrupt.class)
        );
        return configuration;
    }
}