package org.spring.azure.hub.registrar;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spring.azure.hub.model.context.ServiceBusSharedResources;
import org.spring.azure.hub.model.dto.ListenerMethod;
import org.spring.azure.hub.annotation.GlobalDeadLetterHandler;
import org.spring.azure.hub.registrar.validator.MethodValidator;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

@Component @Slf4j @RequiredArgsConstructor
public class GlobalDeadLetterHandlerRegistrar implements BeanPostProcessor {

    private final ServiceBusSharedResources sharedResources;
    private final MethodValidator methodValidator;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Method[] methods = ReflectionUtils.getAllDeclaredMethods(bean.getClass());
        for (Method method : methods)
            if (method.isAnnotationPresent(GlobalDeadLetterHandler.class))
                registerGlobalDeadLetterListener(method, bean);
        return bean;
    }

    protected void registerGlobalDeadLetterListener(Method method, Object bean) {
        methodValidator.validateListenerMethod(method);
        sharedResources.addGlobalDeadListener(new ListenerMethod(bean, method, getOrder(method, bean)));
        log.info("Global dead letter listener registered for method: {} in bean: {}", method.getName(), bean.getClass().getSimpleName());
    }

    protected int getOrder(Method method, Object bean) {
        Order order = AnnotationUtils.findAnnotation(method, Order.class);
        if (order == null)
            order = AnnotationUtils.findAnnotation(bean.getClass(), Order.class);
        return (order != null) ? order.value() : Integer.MAX_VALUE;
    }
}