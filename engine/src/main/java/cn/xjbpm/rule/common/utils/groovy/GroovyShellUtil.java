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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.springframework.util.ClassUtils;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Groovy 脚本工具类 - 解决 parseClass 访问权限问题
 * 使用 Guava 缓存编译后的 Class，防止 Metaspace 溢出
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Slf4j
public class GroovyShellUtil {


    private static final CompilerConfiguration COMPILER_CONFIGURATION = CompilerConfigurationFactory.create();
    private static final GroovyClassLoader GROOVY_CLASS_LOADER = new GroovyClassLoader(ClassUtils.getDefaultClassLoader(), COMPILER_CONFIGURATION);
    /**
     * Guava LRU 缓存：限制最大 500 个脚本
     */
    private static final Cache<String, Class<? extends Script>> SCRIPT_CACHE = CacheBuilder.newBuilder().maximumSize(500).expireAfterAccess(24, TimeUnit.HOURS).removalListener((RemovalListener<String, Class<? extends Script>>) notification -> {
        log.info("Groovy 脚本缓存移除: 原因={}, 脚本长度={}", notification.getCause(), notification.getKey() != null ? notification.getKey().length() : 0);
        GROOVY_CLASS_LOADER.clearCache();
    }).build();



    /**
     * 执行脚本
     */
    @SuppressWarnings("unchecked")
    public static Object runScript(String scriptContent, Map<String, Object> variables) {
        if (scriptContent == null || scriptContent.trim().isEmpty()) {
            return null;
        }

        try {
            // 1. 从缓存获取编译后的 Class
            // 修复点：直接调用 GROOVY_CLASS_LOADER.parseClass(String)，该方法在 Java 中是 public 的
            Class<? extends Script> scriptClass = SCRIPT_CACHE.get(scriptContent, () -> {
                log.info("编译 Groovy 脚本并存入缓存 (Length: {})", scriptContent.length());
                return (Class<? extends Script>) GROOVY_CLASS_LOADER.parseClass(scriptContent);
            });

            // 2. 实例化
            Script scriptObject = scriptClass.getDeclaredConstructor().newInstance();

            // 3. 绑定变量
            Binding binding = new Binding();
            if (variables != null) {
                variables.forEach(binding::setVariable);
            }
            scriptObject.setBinding(binding);

            // 4. 执行
            return scriptObject.run();

        } catch (ExecutionException e) {
            log.error("Groovy 脚本编译失败", e);
            throw new RuntimeException("Groovy 语法错误", e.getCause());
        } catch (Exception e) {
            log.error("Groovy 脚本执行异常", e);
            throw new RuntimeException("Groovy 执行失败", e);
        }
    }

    /**
     * 手动清理
     */
    public static void clearCache() {
        SCRIPT_CACHE.invalidateAll();
        GROOVY_CLASS_LOADER.clearCache();
    }
}