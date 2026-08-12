package com.joven.inventory.audit;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Utility class that provides static access to the Spring ApplicationContext.
 * Used by JPA Entity Listeners that cannot use constructor injection directly.
 *
 * <p>Spring automatically invokes {@link #setApplicationContext(ApplicationContext)}
 * during application startup, making the context available via the static
 * {@link #getBean(Class)} method throughout the application lifecycle.</p>
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Component
public class SpringContext implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    /**
     * Stores the Spring ApplicationContext for static access.
     * Called automatically by the Spring framework during initialization.
     *
     * @param context the Spring ApplicationContext
     * @throws BeansException if an error occurs during context assignment
     */
    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }

    /**
     * Retrieves a Spring-managed bean by its type.
     *
     * @param <T>       the type of the bean
     * @param beanClass the class of the bean to retrieve
     * @return the bean instance
     * @throws IllegalStateException if the ApplicationContext has not been initialized
     * @throws BeansException        if the bean cannot be found or created
     */
    public static <T> T getBean(Class<T> beanClass) {
        if (applicationContext == null) {
            throw new IllegalStateException("ApplicationContext has not been initialized");
        }
        return applicationContext.getBean(beanClass);
    }
}
