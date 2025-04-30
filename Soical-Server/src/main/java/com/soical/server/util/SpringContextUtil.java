package com.soical.server.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Spring上下文工具类
 * 用于在非Spring管理的类中获取Spring Bean
 */
@Component
public class SpringContextUtil implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextUtil.applicationContext = applicationContext;
    }

    /**
     * 获取ApplicationContext
     *
     * @return ApplicationContext
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * 通过名称获取Bean
     *
     * @param name Bean名称
     * @return Bean实例
     */
    public static Object getBean(String name) {
        return applicationContext.getBean(name);
    }

    /**
     * 通过类型获取Bean
     *
     * @param clazz Bean类型
     * @param <T>   Bean类型
     * @return Bean实例
     */
    public static <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }

    /**
     * 通过名称和类型获取Bean
     *
     * @param name  Bean名称
     * @param clazz Bean类型
     * @param <T>   Bean类型
     * @return Bean实例
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return applicationContext.getBean(name, clazz);
    }
} 