package org.spring.azure.hub.settings;

import org.spring.azure.hub.model.Constants;
import org.spring.azure.hub.model.properties.ServiceBusProperties;
import org.spring.azure.hub.settings.client.MessageBusClient;
import org.spring.azure.hub.model.context.ServiceBusSharedResources;
import org.spring.azure.hub.model.dto.ErrorHandlerMethod;
import org.spring.azure.hub.model.dto.ListenerMethod;
import org.spring.azure.hub.model.dto.NameData;
import org.spring.azure.hub.model.dto.TopicKey;
import org.springframework.stereotype.Component;

@Component
public class TopicSettings extends AbstractMessagingSettings<ServiceBusProperties.TopicConfig.SubscriptionConfig, ServiceBusProperties.TopicConfig> {

    public TopicSettings(ServiceBusProperties serviceBusProperties, ServiceBusSharedResources sharedResources, MessageBusClient messageBusClient) {
        super(serviceBusProperties, sharedResources, messageBusClient);
    }

    public void configure(ServiceBusProperties.TopicConfig topicConfig) {
        topicConfig.getSubscriptions().forEach(subscription -> super.initializeEntityConfiguration(subscription, topicConfig));
    }

    @Override
    protected String getEntityName(ServiceBusProperties.TopicConfig.SubscriptionConfig entityConfig, ServiceBusProperties.TopicConfig parentConfig) {
        return parentConfig.getName() + "/" + entityConfig.getName();
    }

    @Override
    protected String getEntityType() {
        return "Subscription";
    }

    @Override
    protected String getDeadLetterName(ServiceBusProperties.TopicConfig.SubscriptionConfig entityConfig, ServiceBusProperties.TopicConfig parentConfig) {
        return entityConfig.getName() + Constants.DEAD_LETTER;
    }

    @Override
    protected ListenerMethod getListener(ServiceBusProperties.TopicConfig.SubscriptionConfig entityConfig, ServiceBusProperties.TopicConfig parentConfig) {
        TopicKey key = new TopicKey(parentConfig.getName(), entityConfig.getName());
        return sharedResources.getTopicListeners().get(key);
    }

    @Override
    protected ErrorHandlerMethod getErrorHandler(ServiceBusProperties.TopicConfig.SubscriptionConfig entityConfig, ServiceBusProperties.TopicConfig parentConfig) {
        TopicKey key = new TopicKey(parentConfig.getName(), entityConfig.getName());
        return sharedResources.getTopicErrorHandlers().getOrDefault(key, sharedResources.getGlobalErrorHandler());
    }

    @Override
    protected ListenerMethod getDeadLetterListener(ServiceBusProperties.TopicConfig.SubscriptionConfig entityConfig, ServiceBusProperties.TopicConfig parentConfig) {
        String deadLetterSubscriptionName = getDeadLetterName(entityConfig, parentConfig);
        TopicKey key = new TopicKey(parentConfig.getName(), deadLetterSubscriptionName);
        return sharedResources.getTopicListeners().getOrDefault(key, sharedResources.getGlobalDLQListener());
    }

    @Override
    protected ErrorHandlerMethod getDeadLetterErrorHandler(ServiceBusProperties.TopicConfig.SubscriptionConfig entityConfig, ServiceBusProperties.TopicConfig parentConfig) {
        String deadLetterSubscriptionName = getDeadLetterName(entityConfig, parentConfig);
        TopicKey key = new TopicKey(parentConfig.getName(), deadLetterSubscriptionName);
        return sharedResources.getTopicErrorHandlers().getOrDefault(key, sharedResources.getGlobalErrorHandler());
    }

    @Override
    protected NameData createNameData(ServiceBusProperties.TopicConfig.SubscriptionConfig entityConfig, ServiceBusProperties.TopicConfig parentConfig) {
        return NameData.builder().topic(parentConfig.getName()).subscription(entityConfig.getName()).build();
    }

    @Override
    protected String getConnectionString(ServiceBusProperties.TopicConfig.SubscriptionConfig entityConfig, ServiceBusProperties.TopicConfig parentConfig) {
        return parentConfig.getConnectionString();
    }

    @Override
    protected String getEntityPath(ServiceBusProperties.TopicConfig.SubscriptionConfig entityConfig, ServiceBusProperties.TopicConfig parentConfig) {
        return parentConfig.getName();
    }
}