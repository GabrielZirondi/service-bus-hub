package org.spring.azure.hub.registrar;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spring.azure.hub.model.Constants;
import org.spring.azure.hub.model.context.ServiceBusSharedResources;
import org.spring.azure.hub.model.dto.TopicKey;
import org.spring.azure.hub.annotation.QueueListener;
import org.spring.azure.hub.annotation.TopicListener;
import org.spring.azure.hub.registrar.validator.MethodValidator;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class ListenerRegistrar implements BeanPostProcessor {

    private final ServiceBusSharedResources sharedResources;
    private final MethodValidator methodValidator;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Method[] methods = ReflectionUtils.getAllDeclaredMethods(bean.getClass());
        for (Method method : methods) {
            if (method.isAnnotationPresent(QueueListener.class))
                registerQueueListener(method, bean);
            else if (method.isAnnotationPresent(TopicListener.class))
                registerTopicListener(method, bean);
        }
        return bean;
    }

    protected void registerQueueListener(Method method, Object bean) {
        QueueListener listenerAnnotation = method.getAnnotation(QueueListener.class);
        String queueName = listenerAnnotation.value();
        if (listenerAnnotation.isDeadLetterQueue())
            queueName += Constants.DEAD_LETTER;
        checkDuplicate(method, bean, queueName);
        methodValidator.validateListenerMethod(method);
        sharedResources.registerListenerMethod(queueName, bean, method);
        log.info("Registered queue listener for '{}': {}#{}", queueName, bean.getClass().getName(), method.getName());
    }

    protected void registerTopicListener(Method method, Object bean) {
        TopicListener listenerAnnotation = method.getAnnotation(TopicListener.class);
        String topicName = listenerAnnotation.topic();
        String subscriptionName = listenerAnnotation.subscription();
        if (listenerAnnotation.isDeadLetterQueue())
            subscriptionName += Constants.DEAD_LETTER;
        methodValidator.validateListenerMethod(method);
        TopicKey key = new TopicKey(topicName, subscriptionName);
        checkDuplicate(method, bean, key);
        sharedResources.registerListenerMethod(key, bean, method);
        log.info("Registered topic listener for '{}':'{}': {}#{}", topicName, subscriptionName, bean.getClass().getName(), method.getName());
    }

    protected void checkDuplicate(Method method, Object bean, TopicKey key) {
        if (!sharedResources.getTopicListeners().containsKey(key)) return;
        String duplicateListenersInfo = sharedResources.getTopicListeners().values().stream()
                .map(listener -> String.format("%s, Method: %s", listener.getBean().getClass(), listener.getMethod().getName()))
                .collect(Collectors.joining(System.lineSeparator()));
        throw new IllegalStateException(
                String.format("Multiple @TopicListener methods found. Duplicate listener detected for queue: '%s'.%n%n%s%n%s, Method: %s",
                        key.getTopic(), duplicateListenersInfo, bean.getClass(), method.getName()));
    }

    protected void checkDuplicate(Method method, Object bean, String queueName) {
        if (!sharedResources.getQueueListeners().containsKey(queueName)) return;
        String duplicateListenersInfo = sharedResources.getQueueListeners().values().stream()
                .map(listener -> String.format("%s, Method: %s", listener.getBean().getClass(), listener.getMethod().getName()))
                .collect(Collectors.joining(System.lineSeparator()));
        throw new IllegalStateException(
                String.format("Multiple @QueueListener methods found. Duplicate listener detected for queue: '%s'.%n%n%s%n%s, Method: %s",
                        queueName, duplicateListenersInfo, bean.getClass(), method.getName())
        );
    }
}