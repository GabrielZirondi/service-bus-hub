package org.spring.azure.hub.registrar;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spring.azure.hub.model.context.ServiceBusSharedResources;
import org.spring.azure.hub.utilities.SerializerUtil;
import org.spring.azure.hub.settings.processor.serialization.ServiceBusSerializer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

@Component @Slf4j @RequiredArgsConstructor
public class SerializerRegistrar implements BeanPostProcessor {

    private final ServiceBusSharedResources sharedResources;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (!(bean instanceof ServiceBusSerializer)) return bean;
        ServiceBusSerializer<?> serializer = (ServiceBusSerializer<?>) bean;
        Class<?> targetType = SerializerUtil.getClassFromSerializer(serializer);
        sharedResources.getSerializerMap().put(targetType, serializer);
        log.info("Registered {} serializer for type: {}", serializer.getClass(), targetType.getName());
        return bean;
    }
}