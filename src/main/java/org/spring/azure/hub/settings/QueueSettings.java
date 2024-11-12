package org.spring.azure.hub.settings;

import org.spring.azure.hub.model.Constants;
import org.spring.azure.hub.model.properties.ServiceBusProperties;
import org.spring.azure.hub.settings.client.MessageBusClient;
import org.spring.azure.hub.model.context.ServiceBusSharedResources;
import org.spring.azure.hub.model.dto.ErrorHandlerMethod;
import org.spring.azure.hub.model.dto.ListenerMethod;
import org.spring.azure.hub.model.dto.NameData;
import org.springframework.stereotype.Component;

@Component
public class QueueSettings extends AbstractMessagingSettings<ServiceBusProperties.QueueConfig, Void> {

    public QueueSettings(ServiceBusProperties serviceBusProperties, ServiceBusSharedResources sharedResources, MessageBusClient messageBusClient) {
        super(serviceBusProperties, sharedResources, messageBusClient);
    }

    public void configure(ServiceBusProperties.QueueConfig queueConfig) {
        super.initializeEntityConfiguration(queueConfig, null);
    }

    @Override
    protected String getEntityName(ServiceBusProperties.QueueConfig entityConfig, Void parentConfig) {
        return entityConfig.getName();
    }

    @Override
    protected String getEntityType() {
        return "Queue";
    }

    @Override
    protected String getDeadLetterName(ServiceBusProperties.QueueConfig entityConfig, Void parentConfig) {
        return entityConfig.getName() + Constants.DEAD_LETTER;
    }

    @Override
    protected ListenerMethod getListener(ServiceBusProperties.QueueConfig entityConfig, Void parentConfig) {
        return sharedResources.getQueueListeners().get(entityConfig.getName());
    }

    @Override
    protected ErrorHandlerMethod getErrorHandler(ServiceBusProperties.QueueConfig entityConfig, Void parentConfig) {
        return sharedResources.getQueueErrorHandlers().getOrDefault(entityConfig.getName(), sharedResources.getGlobalErrorHandler());
    }

    @Override
    protected ListenerMethod getDeadLetterListener(ServiceBusProperties.QueueConfig entityConfig, Void parentConfig) {
        String deadLetterQueueName = getDeadLetterName(entityConfig, null);
        return sharedResources.getQueueListeners().getOrDefault(deadLetterQueueName, sharedResources.getGlobalDLQListener());
    }

    @Override
    protected ErrorHandlerMethod getDeadLetterErrorHandler(ServiceBusProperties.QueueConfig entityConfig, Void parentConfig) {
        String deadLetterQueueName = getDeadLetterName(entityConfig, null);
        return sharedResources.getQueueErrorHandlers().getOrDefault(deadLetterQueueName, sharedResources.getGlobalErrorHandler());
    }

    @Override
    protected NameData createNameData(ServiceBusProperties.QueueConfig entityConfig, Void parentConfig) {
        return NameData.builder().queue(entityConfig.getName()).build();
    }

    @Override
    protected String getConnectionString(ServiceBusProperties.QueueConfig entityConfig, Void parentConfig) {
        return entityConfig.getConnectionString();
    }

    @Override
    protected String getEntityPath(ServiceBusProperties.QueueConfig entityConfig, Void parentConfig) {
        return entityConfig.getName();
    }
}
