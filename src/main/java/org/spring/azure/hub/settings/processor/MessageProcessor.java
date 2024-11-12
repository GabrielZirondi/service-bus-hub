package org.spring.azure.hub.settings.processor;

import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spring.azure.hub.model.context.ServiceBusSharedResources;
import org.spring.azure.hub.model.dto.ErrorHandlerMethod;
import org.spring.azure.hub.model.dto.ListenerMethod;
import org.spring.azure.hub.settings.processor.serialization.Serializer;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Arrays;

@Slf4j @RequiredArgsConstructor @Component
public class MessageProcessor {

    private final ServiceBusSharedResources sharedResources;
    private final ObjectMapper serviceBusClientObjectMapper;

    public void processMessage(String name,
                               ListenerMethod listener,
                               ServiceBusReceivedMessageContext message,
                               boolean isConditional, Boolean abandonOnSerializationError) {
        log.debug("Processing message from '{}'", name);
        Method listenerMethod = listener.getMethod();
        Class<?>[] parameterTypes = listenerMethod.getParameterTypes();
        boolean autoAck = isAutoAck(parameterTypes, isConditional);
        Object[] args = getParameters(name, message, parameterTypes, listenerMethod, abandonOnSerializationError, autoAck);
        try {
            ReflectionUtils.invokeMethod(listenerMethod, listener.getBean(), args);
            if (autoAck) {
                message.complete();
                log.debug("Message successfully completed for '{}'", name);
            }
        } catch (Throwable e) {
            if (autoAck)
                message.abandon();
            throw e;
        }
    }

    public void handleError(ErrorHandlerMethod errorHandler, ServiceBusErrorContext context) {
        log.debug("Invoking Error Handler '{}'", errorHandler.getMethod());
        ReflectionUtils.invokeMethod(errorHandler.getMethod(), errorHandler.getBean(), context);
    }

    protected boolean isAutoAck(Class<?>[] parameterTypes, boolean isConditional) {
        return isConditional && parameterTypes.length == 1 && !ServiceBusReceivedMessageContext.class.isAssignableFrom(parameterTypes[0]);
    }

    protected Object[] getParameters(String name, ServiceBusReceivedMessageContext message,
                                   Class<?>[] parameterTypes, Method listenerMethod,
                                   boolean abandonOnSerializationError, boolean autoAck) {
        try {
            Object deserializedObject = Arrays.stream(parameterTypes)
                    .filter(paramType -> !ServiceBusReceivedMessageContext.class.isAssignableFrom(paramType))
                    .findFirst()
                    .map(type -> Serializer.serialize(type, message, serviceBusClientObjectMapper, sharedResources))
                    .orElse(message);
            Object[] parameters = prepareListenerArgs(parameterTypes, deserializedObject, message);
            log.debug("Invoking Listener '{}' for '{}'", listenerMethod.getName(), name);
            return parameters;
        } catch (Throwable e) {
            if(autoAck || abandonOnSerializationError)
                message.abandon();
            throw e;
        }
    }

    protected Object[] prepareListenerArgs(Class<?>[] parameterTypes, Object deserializedObject, ServiceBusReceivedMessageContext message) {
        if (parameterTypes.length == 2 && Arrays.stream(parameterTypes).anyMatch(ServiceBusReceivedMessageContext.class::isAssignableFrom))
            return new Object[]{deserializedObject, message};
        else if (parameterTypes.length == 1 && ServiceBusReceivedMessageContext.class.isAssignableFrom(parameterTypes[0]))
            return new Object[]{message};
        else
            return new Object[]{deserializedObject};
    }
}