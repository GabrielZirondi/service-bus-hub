package org.spring.azure.hub.initializer;

import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.spring.azure.hub.initializer.configurer.QueueConfigurer;
import org.spring.azure.hub.initializer.configurer.TopicConfigurer;
import org.spring.azure.hub.initializer.validator.ListenerValidator;
import org.spring.azure.hub.model.context.ServiceBusSharedResources;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.Map;

@Component @Slf4j @RequiredArgsConstructor
public class ServiceBusClientInitializer implements SmartLifecycle {

    private final ListenerValidator listenerValidator;
    private final QueueConfigurer queueConfigurer;
    private final TopicConfigurer topicConfigurer;
    private final ServiceBusSharedResources sharedResources;
    private boolean running = false;

    @Override
    public void start() {
        log.info("Starting Service Bus client initializer.");
        listenerValidator.validate();
        log.info("Configuring Service Bus clients for queues.");
        queueConfigurer.configureQueues();
        log.info("Configuring Service Bus clients for topics and subscriptions.");
        topicConfigurer.configureTopics();
        startProcessors();
        log.info("Service Bus client creation and initialization completed successfully.");
        running = true;
    }

    @Override
    public void stop() {
        log.info("Stopping Service Bus client initializer.");
        shutdown();
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE - 1;
    }

    protected void startProcessors() {
        Map<String, ServiceBusProcessorClient> processorClients = sharedResources.getProcessorClients();
        int processorCount = processorClients.size();
        log.info("Starting '{}' Service Bus processors.", processorCount);
        processorClients.forEach((entityName, processorClient) -> {
            processorClient.start();
            log.info("Started Service Bus processor for entity: '{}'", entityName);
        });
    }

    @PreDestroy
    public void shutdown() {
        log.info("Application shutdown process initiated. Closing Service Bus processor clients.");
        sharedResources.getProcessorClients().values().forEach(client -> {
            log.debug("Closing Service Bus processor client: {}", client);
            client.close();
        });
    }
}