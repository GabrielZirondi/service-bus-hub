package org.spring.azure.hub.utilities;

import org.spring.azure.hub.settings.processor.serialization.ServiceBusSerializer;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public @Slf4j @UtilityClass class SerializerUtil {

    public static Class<?> getClassFromSerializer(Object instance) {
        Class<?> clazz = instance.getClass();
        Type[] genericInterfaces = clazz.getGenericInterfaces();
        for (Type genericInterface : genericInterfaces)
            if (genericInterface instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) genericInterface;
                if (parameterizedType.getRawType() == ServiceBusSerializer.class)
                    return (Class<?>) parameterizedType.getActualTypeArguments()[0];
            }
        throw new IllegalArgumentException(
                String.format("Unable to determine the generic type from serializer: %s. Ensure the serializer implements ServiceBusSerializer with a valid generic type.",
                        instance.getClass().getName())
        );
    }
}