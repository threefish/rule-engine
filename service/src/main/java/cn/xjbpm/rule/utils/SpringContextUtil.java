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

package cn.xjbpm.rule.utils;

import cn.hutool.core.util.StrUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.context.ContextLoader;

import java.util.Map;
import java.util.Objects;

/**
 * @author 黄川 huchuc@vip.qq.com
 */
@Component
public class SpringContextUtil implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    public static boolean hasBean(String beanName) {
        try {
            return Objects.nonNull(getBean(beanName));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 根据名称获取bean
     *
     * @param beanName
     * @return
     */
    public static Object getBean(String beanName) {
        return getApplicationContext() == null ? null : getApplicationContext().getBean(beanName);
    }

    public static ApplicationContext getApplicationContext() {
        if (applicationContext == null) {
            applicationContext = ContextLoader.getCurrentWebApplicationContext();
        }
        return applicationContext;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextUtil.applicationContext = applicationContext;
    }

    public static void registerSingleton(String beanName, Object bean) {
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
        beanFactory.registerSingleton(beanName, bean);
    }

    public static void destroySingleton(String beanName) {
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
        beanFactory.destroySingleton(beanName);
    }

    /**
     * 根据名称和类型获取bean
     *
     * @param beanName
     * @param beanType
     * @param <T>
     * @return
     */
    public static <T> T getBean(String beanName, Class<T> beanType) {
        return getApplicationContext() == null ? null : getApplicationContext().getBean(beanName, beanType);
    }

    /**
     * 根据类型获取所有bean
     *
     * @param beanType
     * @param <T>
     * @return
     */
    public static <T> Map<String, T> getBeans(Class<T> beanType) {
        return getApplicationContext() == null ? null : getApplicationContext().getBeansOfType(beanType);
    }

    /**
     * 获取属性
     *
     * @param propertyKey
     * @return
     */
    public static String getProperty(String propertyKey) {
        return getProperty(propertyKey, null);
    }

    /**
     * 获取属性
     *
     * @param propertyKey
     * @param defaultValue
     * @return
     */
    public static String getProperty(String propertyKey, String defaultValue) {
        if (StrUtil.isBlank(propertyKey)) {
            return null;
        }
        Environment environment = getBean(Environment.class);
        Assert.notNull(applicationContext, "Srping Environment is null");
        final String regex = ":";
        String key = propertyKey;
        String defaultReturnValue = defaultValue;
        if (isSpringValue(propertyKey)) {
            key = propertyKey.substring(2, propertyKey.length() - 1);
            if (propertyKey.contains(regex)) {
                String[] keyAndDefaultValue = key.split(regex);
                key = keyAndDefaultValue[0];
                defaultReturnValue = keyAndDefaultValue[1];
            }
        }
        return environment.getProperty(key, defaultReturnValue);
    }

    /**
     * 根据类型获取bean
     *
     * @param beanType
     * @param <T>
     * @return
     */
    public static <T> T getBean(Class<T> beanType) {
        return getApplicationContext() == null ? null : getApplicationContext().getBean(beanType);
    }

    /**
     * 是否是 spring value 表达式
     *
     * @param key
     * @return
     */
    public static Boolean isSpringValue(String key) {
        return key != null && (key.startsWith("${") || key.startsWith("#{") && key.endsWith("}"));
    }

}