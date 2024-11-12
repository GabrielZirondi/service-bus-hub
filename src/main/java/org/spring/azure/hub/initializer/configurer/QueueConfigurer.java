package org.spring.azure.hub.initializer.configurer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spring.azure.hub.model.Constants;
import org.spring.azure.hub.model.properties.ServiceBusProperties;
import org.spring.azure.hub.settings.QueueSettings;
import org.spring.azure.hub.model.context.ServiceBusSharedResources;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component @Slf4j @RequiredArgsConstructor
public class QueueConfigurer {

    private final ServiceBusProperties serviceBusProperties;
    private final ServiceBusSharedResources sharedResources;
    private final QueueSettings queueSettings;

    public void configureQueues() {
        addMissingQueues();
        configureQueueSettings();
    }

    protected void addMissingQueues() {
        Set<String> existingQueueNames = serviceBusProperties.getQueues().stream()
                .map(ServiceBusProperties.QueueConfig::getName)
                .collect(Collectors.toSet());

        sharedResources.getQueueListeners().keySet().stream()
                .filter(queueName -> !queueName.endsWith(Constants.DEAD_LETTER))
                .filter(queueName -> !existingQueueNames.contains(queueName))
                .forEach(queueName -> {
                    ServiceBusProperties.QueueConfig queue = ServiceBusProperties.QueueConfig.builder().name(queueName).build();
                    queue.inheritPropertiesFrom(serviceBusProperties);
                    serviceBusProperties.getQueues().add(queue);
                    log.debug("Added missing queue configuration for '{}'", queueName);
                });
    }

    protected void configureQueueSettings() {
        for (ServiceBusProperties.QueueConfig queueConfig : serviceBusProperties.getQueues()) {
            log.debug("Configuring Service Bus client for queue: {}", queueConfig.getName());
            queueSettings.configure(queueConfig);
        }
    }
}
