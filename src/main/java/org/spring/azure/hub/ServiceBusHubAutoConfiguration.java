package org.spring.azure.hub;

import org.spring.azure.hub.initializer.ServiceBusClientInitializer;
import org.spring.azure.hub.initializer.configurer.QueueConfigurer;
import org.spring.azure.hub.initializer.configurer.TopicConfigurer;
import org.spring.azure.hub.initializer.validator.ListenerValidator;
import org.spring.azure.hub.model.context.ServiceBusSharedResources;
import org.spring.azure.hub.model.properties.ServiceBusProperties;
import org.spring.azure.hub.provider.ServiceBusConnectionProvider;
import org.spring.azure.hub.provider.ServiceBusObjectMapperConfig;
import org.spring.azure.hub.registrar.ErrorHandlerRegistrar;
import org.spring.azure.hub.registrar.GlobalDeadLetterHandlerRegistrar;
import org.spring.azure.hub.registrar.ListenerRegistrar;
import org.spring.azure.hub.registrar.SerializerRegistrar;
import org.spring.azure.hub.registrar.validator.MethodValidator;
import org.spring.azure.hub.settings.QueueSettings;
import org.spring.azure.hub.settings.TopicSettings;
import org.spring.azure.hub.settings.client.MessageBusClient;
import org.spring.azure.hub.settings.processor.MessageProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        MethodValidator.class,
        ErrorHandlerRegistrar.class,
        GlobalDeadLetterHandlerRegistrar.class,
        ListenerRegistrar.class,
        SerializerRegistrar.class,
        QueueConfigurer.class,
        TopicConfigurer.class,
        ListenerValidator.class,
        ServiceBusClientInitializer.class,
        ServiceBusProperties.class,
        ServiceBusSharedResources.class,
        QueueSettings.class,
        TopicSettings.class,
        MessageProcessor.class,
        ServiceBusConnectionProvider.class,
        MessageBusClient.class,
        QueueSettings.class,
        ServiceBusObjectMapperConfig.class,
        TopicSettings.class
})
public class ServiceBusHubAutoConfiguration {
}