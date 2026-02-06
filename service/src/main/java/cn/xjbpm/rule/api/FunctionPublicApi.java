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

package cn.xjbpm.rule.api;

import cn.hutool.core.util.StrUtil;
import cn.xjbpm.rule.common.utils.ClassScanner;
import cn.xjbpm.rule.engine.aviator.AviatorExecutor;
import cn.xjbpm.rule.engine.aviator.annotation.FunctionDoc;
import cn.xjbpm.rule.engine.aviator.annotation.FunctionNamespace;
import cn.xjbpm.rule.engine.aviator.function.AbstractBaseFunction;
import cn.xjbpm.rule.engine.aviator.function.AviatorExtendFunction;
import cn.xjbpm.rule.vo.common.ResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author 黄川 huchuc@vip.qq.com
 */
@RestController
@RequestMapping("/public/ruleflow")
@RequiredArgsConstructor
@Slf4j
public class FunctionPublicApi {


    @PostMapping("/functions")
    public ResultVO<List<AviatorExtendFunction>> functions() {
        ClassScanner classScanner = new ClassScanner(AviatorExecutor.class.getPackage().getName());
        Set<Class<?>> scans = classScanner.scan();
        List<Class<?>> collect = scans.stream()
                .filter(clazz -> AbstractBaseFunction.class.isAssignableFrom(clazz))
                .filter(clazz -> !Modifier.isAbstract(clazz.getModifiers()))
                .filter(clazz -> !Modifier.isInterface(clazz.getModifiers()))
                .collect(Collectors.toList());
        List<AviatorExtendFunction> list = new ArrayList<>();
        for (Class<?> clazz : collect) {
            try {
                AbstractBaseFunction aviatorFunction = (AbstractBaseFunction) clazz.getDeclaredConstructor().newInstance();
                list.addAll(aviatorFunction.docs());
            } catch (Exception e) {
                log.warn("error for {}", clazz.getName(), e);
            }
        }
        List<Class<?>> aviatorFunctionClasses = scans.stream()
                .filter(clazz -> clazz.isAnnotationPresent(FunctionNamespace.class))
                .collect(Collectors.toList());
        for (Class<?> clazz : aviatorFunctionClasses) {
            // 获取类级别的注解，拿到命名空间（如 "url"）
            FunctionNamespace aviatorFunction = clazz.getAnnotation(FunctionNamespace.class);
            String namespace = aviatorFunction.name();
            // 遍历该类下的所有公共方法
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                // 只处理带有 @FunctionDoc 注ic方法
                if (method.isAnnotationPresent(FunctionDoc.class)) {
                    FunctionDoc doc = method.getAnnotation(FunctionDoc.class);
                    String functionValue = namespace + "." + method.getName();
                    String description = doc.description();
                    if (description == null || description.trim().isEmpty()) {
                        description = method.getReturnType().getSimpleName();
                    }
                    AviatorExtendFunction extendFunction = new AviatorExtendFunction(
                            functionValue,
                            StrUtil.isNotBlank(doc.value()) ? doc.value() : functionValue,
                            doc.response(),
                            description,
                            doc.example(),
                            doc.link()
                    );
                    list.add(extendFunction);
                }
            }
        }
        return ResultVO.success(list);
    }
}