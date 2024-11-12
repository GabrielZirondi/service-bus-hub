package org.spring.azure.hub.registrar;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spring.azure.hub.model.context.ServiceBusSharedResources;
import org.spring.azure.hub.model.dto.ErrorHandlerMethod;
import org.spring.azure.hub.model.dto.TopicKey;
import org.spring.azure.hub.registrar.resolver.AnnotationOrderResolver;
import org.spring.azure.hub.registrar.validator.DuplicateErrorHandlerException;
import org.spring.azure.hub.registrar.validator.MethodValidator;
import org.spring.azure.hub.annotation.ErrorHandler;
import org.spring.azure.hub.annotation.GlobalErrorHandler;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

@Component @Slf4j @RequiredArgsConstructor
public class ErrorHandlerRegistrar implements BeanPostProcessor {

    private final ServiceBusSharedResources sharedResources;
    private final MethodValidator methodValidator;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Method[] methods = ReflectionUtils.getAllDeclaredMethods(bean.getClass());
        for (Method method : methods) {
            if (method.isAnnotationPresent(GlobalErrorHandler.class))
                registerGlobalErrorHandler(method, bean);
            else if (method.isAnnotationPresent(ErrorHandler.class))
                registerErrorHandler(method, bean);
        }
        return bean;
    }

    protected void registerGlobalErrorHandler(Method method, Object bean) {
        methodValidator.validateErrorHandlerMethod(method);
        int order = AnnotationOrderResolver.resolve(method, bean);
        ErrorHandlerMethod errorHandlerMethod = new ErrorHandlerMethod(bean, method, order);
        sharedResources.addGlobalErrorHandler(errorHandlerMethod);
        log.info("Registered global error handler: {}#{}", bean.getClass().getName(), method.getName());
    }

    protected void registerErrorHandler(Method method, Object bean) {
        ErrorHandler annotation = method.getAnnotation(ErrorHandler.class);
        methodValidator.validateErrorHandlerMethod(method);
        String queueName = annotation.queue();
        if (!queueName.isEmpty()) {
            if (sharedResources.getQueueErrorHandlers().containsKey(queueName))
                throw new DuplicateErrorHandlerException(queueName, method, bean);
            sharedResources.registerErrorHandlerMethod(queueName, bean, method);
            log.info("Registered error handler for queueName '{}': {}#{}", queueName, bean.getClass().getName(), method.getName());
        } else if (!annotation.topic().isEmpty() && !annotation.subscription().isEmpty()) {
            TopicKey key = new TopicKey(annotation.topic(), annotation.subscription());
            if (sharedResources.getTopicErrorHandlers().containsKey(key))
                throw new DuplicateErrorHandlerException(key.toString(), method, bean);
            sharedResources.registerErrorHandlerMethod(key, bean, method);
            log.info("Registered error handler for topic '{}', subscription '{}': {}#{}", annotation.topic(), annotation.subscription(), bean.getClass().getName(), method.getName());
        } else
            throw new IllegalArgumentException("ErrorHandler annotation must specify either a queueName or both topic and subscription.");
    }
}