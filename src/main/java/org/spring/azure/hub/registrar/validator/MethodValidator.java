package org.spring.azure.hub.registrar.validator;

import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

@Component
public class MethodValidator {

    public void validateListenerMethod(Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length < 1 || parameterTypes.length > 2)
            throw new IllegalArgumentException(String.format(
                    "Listener method '%s' must have 1 or 2 parameters.", method.getName()));
        if (parameterTypes.length == 2 &&
                Arrays.stream(parameterTypes).noneMatch(parameter -> parameter.isAssignableFrom(ServiceBusReceivedMessageContext.class)))
            throw new IllegalArgumentException(String.format(
                    "Listener method '%s' in class '%s' has an invalid signature. If two parameters are specified, one of them must be of type ServiceBusReceivedMessageContext.",
                    method.getName(), method.getDeclaringClass().getSimpleName()));
    }

    public void validateErrorHandlerMethod(Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length != 1 || !ServiceBusErrorContext.class.isAssignableFrom(parameterTypes[0]))
            throw new IllegalArgumentException(String.format(
                    "Error handler method '%s' must have exactly one parameter of type ServiceBusErrorContext.", method.getName()));
    }
}