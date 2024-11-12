package org.spring.azure.hub.model.context;

import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import org.spring.azure.hub.model.dto.TopicKey;
import org.spring.azure.hub.model.dto.ErrorHandlerMethod;
import org.spring.azure.hub.model.dto.ListenerMethod;
import org.spring.azure.hub.settings.processor.serialization.ServiceBusSerializer;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.*;

@Getter @Setter @Component
public class ServiceBusSharedResources {

    private Map<String, ListenerMethod> queueListeners = new HashMap<>();
    private Map<TopicKey, ListenerMethod> topicListeners = new HashMap<>();
    private Map<String, ErrorHandlerMethod> queueErrorHandlers = new HashMap<>();
    private Map<TopicKey, ErrorHandlerMethod> topicErrorHandlers = new HashMap<>();
    private Map<Class<?>, ServiceBusSerializer<?>> serializerMap = new HashMap<>();
    private Map<String, ServiceBusProcessorClient> processorClients = new HashMap<>();
    private List<ErrorHandlerMethod> globalErrorHandlers = new ArrayList<>();
    private List<ListenerMethod> globalDLQListeners = new ArrayList<>();
    private ErrorHandlerMethod globalErrorHandler;
    private ListenerMethod globalDLQListener;

    public void registerListenerMethod(String queueName, Object bean, Method method) {
        queueListeners.computeIfAbsent(queueName, k -> new ListenerMethod(bean, method, 1));
    }

    public void registerListenerMethod(TopicKey key, Object bean, Method method) {
        topicListeners.computeIfAbsent(key, k -> new ListenerMethod(bean, method, 1));
    }

    public void registerErrorHandlerMethod(String queueName, Object bean, Method method) {
        queueErrorHandlers.computeIfAbsent(queueName, k -> new ErrorHandlerMethod(bean, method, 1));
    }

    public void registerErrorHandlerMethod(TopicKey key, Object bean, Method method) {
        topicErrorHandlers.computeIfAbsent(key, k -> new ErrorHandlerMethod(bean, method, 1));
    }

    public void addGlobalErrorHandler(ErrorHandlerMethod handler) {
        globalErrorHandlers.add(handler);
        globalErrorHandlers.sort(Comparator.comparingInt(ErrorHandlerMethod::getOrder));
        globalErrorHandler = globalErrorHandlers.get(0);
    }

    public void addGlobalDeadListener(ListenerMethod listenerMethod) {
        globalDLQListeners.add(listenerMethod);
        globalDLQListeners.sort(Comparator.comparingInt(ListenerMethod::getOrder));
        globalDLQListener = globalDLQListeners.get(0);
    }
}