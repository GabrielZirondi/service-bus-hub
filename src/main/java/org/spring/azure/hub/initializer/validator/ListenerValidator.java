package org.spring.azure.hub.initializer.validator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spring.azure.hub.model.context.ServiceBusSharedResources;
import org.spring.azure.hub.model.dto.TopicKey;
import org.spring.azure.hub.model.properties.ServiceBusProperties;
import org.springframework.stereotype.Component;

@Component @Slf4j @RequiredArgsConstructor
public class ListenerValidator {

    private final ServiceBusProperties serviceBusProperties;
    private final ServiceBusSharedResources sharedResources;

    public void validate() {
        log.debug("Validating registered listeners and error handlers before configuring Service Bus clients.");
        validateQueueListeners();
        validateTopicListeners();
        log.debug("Validation of all registered listeners and error handlers completed successfully.");
    }

    protected void validateQueueListeners() {
        for (ServiceBusProperties.QueueConfig queue : serviceBusProperties.getQueues()) {
            String queueName = queue.getName();
            if (!sharedResources.getQueueListeners().containsKey(queueName))
                throw new IllegalStateException(String.format(
                        "Missing @QueueListener method for queue: '%s'. Listener registration is required.", queueName));
            log.debug("Validated listener for queue '{}'", queueName);
        }
    }

    protected void validateTopicListeners() {
        for (ServiceBusProperties.TopicConfig topic : serviceBusProperties.getTopics()) {
            String topicName = topic.getName();
            for (ServiceBusProperties.TopicConfig.SubscriptionConfig subscription : topic.getSubscriptions()) {
                String subscriptionName = subscription.getName();
                TopicKey key = new TopicKey(topicName, subscriptionName);
                if (!sharedResources.getTopicListeners().containsKey(key))
                    throw new IllegalStateException(String.format(
                            "Missing @TopicListener method for '%s'. Listener registration is required.", key));
                log.debug("Validated listener for '{}'", key);
            }
        }
    }
}