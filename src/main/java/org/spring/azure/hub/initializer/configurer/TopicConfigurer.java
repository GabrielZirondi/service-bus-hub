package org.spring.azure.hub.initializer.configurer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spring.azure.hub.model.Constants;
import org.spring.azure.hub.settings.TopicSettings;
import org.spring.azure.hub.model.context.ServiceBusSharedResources;
import org.spring.azure.hub.model.properties.ServiceBusProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

@Component @Slf4j @RequiredArgsConstructor
public class TopicConfigurer {

    private final ServiceBusProperties serviceBusProperties;
    private final ServiceBusSharedResources sharedResources;
    private final TopicSettings topicSettings;

    public void configureTopics() {
        addMissingTopicsAndSubscriptions();
        configureTopicSettings();
    }

    protected void addMissingTopicsAndSubscriptions() {
        Map<String, ServiceBusProperties.TopicConfig> existingTopicsMap = serviceBusProperties.getTopics().stream()
                .collect(Collectors.toMap(ServiceBusProperties.TopicConfig::getName, topicConfig -> topicConfig));

        sharedResources.getTopicListeners().keySet().forEach(topicKey -> {
            ServiceBusProperties.TopicConfig topicConfig = existingTopicsMap.computeIfAbsent(
                    topicKey.getTopic(),
                    topicName -> {
                        ServiceBusProperties.TopicConfig newTopicConfig = ServiceBusProperties.TopicConfig.builder()
                                .name(topicName)
                                .subscriptions(new ArrayList<>())
                                .build();
                        newTopicConfig.inheritPropertiesFrom(serviceBusProperties);
                        serviceBusProperties.getTopics().add(newTopicConfig);
                        log.debug("Added missing topic configuration for '{}'", topicName);
                        return newTopicConfig;
                    });
            if(topicKey.getSubscription().endsWith(Constants.DEAD_LETTER)) return;
            boolean subscriptionExists = topicConfig.getSubscriptions().stream()
                    .anyMatch(sub -> sub.getName().equals(topicKey.getSubscription()));

            if (!subscriptionExists) {
                ServiceBusProperties.TopicConfig.SubscriptionConfig subscription =
                        ServiceBusProperties.TopicConfig.SubscriptionConfig.builder().name(topicKey.getSubscription()).build();
                subscription.inheritPropertiesFrom(topicConfig);
                subscription.inheritPropertiesFrom(serviceBusProperties);
                topicConfig.getSubscriptions().add(subscription);
                log.debug("Added missing subscription '{}' for topic '{}'", topicKey.getSubscription(), topicKey.getTopic());
            }
        });
    }

    protected void configureTopicSettings() {
        for (ServiceBusProperties.TopicConfig topicConfig : serviceBusProperties.getTopics()) {
            log.debug("Configuring Service Bus client for topic: {}", topicConfig.getName());
            topicSettings.configure(topicConfig);
        }
    }
}