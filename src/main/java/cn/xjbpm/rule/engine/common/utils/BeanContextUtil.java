package cn.xjbpm.rule.engine.common.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.context.ContextLoader;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * @author 黄川 huchuc@vip.qq.com
 */
@Slf4j
@Component
public class BeanContextUtil implements ApplicationContextAware {

    /**
     * 应用程序上下文
     */
    private static ApplicationContext applicationContext;

    /**
     * 返回应用程序上下文
     *
     * @return
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * spring 设置应用程序上下文
     *
     * @param applicationContext
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (applicationContext == null) {
            applicationContext = ContextLoader.getCurrentWebApplicationContext();
        }
        Assert.notNull(applicationContext, "Srping applicationContext is null");
        BeanContextUtil.applicationContext = applicationContext;
        log.info("BeanContextUtil initialize successfully");
    }

    /**
     * 根据类型获取 bean
     *
     * @param beanType
     * @param <T>
     * @return
     */
    public static <T> T getBean(Class<T> beanType) {
        return getApplicationContext().getBean(beanType);
    }

    /**
     * 根据beanName获取 bean
     *
     * @param beanName
     * @return
     */
    public static Object getBean(String beanName) {
        return getApplicationContext().getBean(beanName);
    }

    /**
     * 根据beanName和 class 类型取得bean
     *
     * @param beanName
     * @param beanType
     * @param <T>
     * @return
     */
    public static <T> T getBeanOfNameAndType(String beanName, Class<T> beanType) {
        return getApplicationContext().getBean(beanName, beanType);
    }

    /**
     * 取得全部指定 class 类型的bean
     *
     * @param beanType
     * @param <T>
     * @return
     */
    public static <T> Map<String, T> getBeansMapOfType(Class<T> beanType) {
        return getApplicationContext().getBeansOfType(beanType);
    }

    /**
     * 取得全部指定 class 类型的bean
     *
     * @param beanType
     * @param <T>
     * @return
     */
    public static <T> List<T> getBeansOfType(Class<T> beanType) {
        final Map<String, T> beans = getBeansMapOfType(beanType);
        if (CollUtil.isEmpty(beans)) {
            return Collections.EMPTY_LIST;
        } else {
            List<T> beanList = new ArrayList(beans.size());
            beanList.addAll(beans.values());
            return beanList;
        }
    }

    /**
     * 获取全部Bean定义名称
     *
     * @return
     */
    public static String[] getBeanNames() {
        return getApplicationContext() == null ? new String[0] : getApplicationContext().getBeanDefinitionNames();
    }

    /**
     * 从Bean中查找注解
     *
     * @param annotationClass
     * @param <T>
     * @return
     */
    public static <T extends Annotation> T getBeanOfTypeAnnotationType(Class<T> annotationClass) {
        List beansWithAnnotation = getBeansOfAnnotation(annotationClass);
        if (CollUtil.isNotEmpty(beansWithAnnotation)) {
            Object next = beansWithAnnotation.iterator().next();
            return AnnotationUtils.findAnnotation(next.getClass(), annotationClass);
        } else {
            return null;
        }
    }

    /**
     * 通过注解列表查找[带注解的bean列表]，再通过[带注解的bean列表]查找bean上全部的注解
     *
     * @param annotationClass
     * @param <T>             带注解的bean列表上的全部注解
     * @return
     */
    public static <T extends Annotation> List<T> getBeansOfAnnotationType(Class<T> annotationClass) {
        List beansWithAnnotation = getBeansOfAnnotation(annotationClass);
        List<T> annotations = new ArrayList<>();
        if (CollUtil.isNotEmpty(beansWithAnnotation)) {
            Iterator iterator = beansWithAnnotation.iterator();
            while (iterator.hasNext()) {
                Object bean = iterator.next();
                annotations.add(AnnotationUtils.findAnnotation(bean.getClass(), annotationClass));
            }
        }
        return annotations;
    }

    /**
     * 通过注解查找 bean
     *
     * @param annotationClass
     * @return
     */
    public static <T> List<T> getBeansOfAnnotation(Class<? extends Annotation> annotationClass) {
        ApplicationContext applicationContext = getApplicationContext();
        if (applicationContext == null) {
            return Collections.EMPTY_LIST;
        } else {
            Map beanMaps = applicationContext.getBeansWithAnnotation(annotationClass);
            return (CollUtil.isEmpty(beanMaps) ? Collections.EMPTY_LIST : new ArrayList(beanMaps.values()));
        }
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
     * 发布事件
     *
     * @param event
     * @return
     */
    public static void publishEvent(Object event) {
        applicationContext.publishEvent(event);
    }

}
