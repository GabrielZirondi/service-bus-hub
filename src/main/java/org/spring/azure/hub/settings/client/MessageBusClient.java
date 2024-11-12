package org.spring.azure.hub.settings.client;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.azure.messaging.servicebus.models.SubQueue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spring.azure.hub.model.Constants;
import org.spring.azure.hub.model.context.ServiceBusSharedResources;
import org.spring.azure.hub.model.dto.ErrorHandlerMethod;
import org.spring.azure.hub.model.dto.ListenerMethod;
import org.spring.azure.hub.model.properties.CompletionMode;
import org.spring.azure.hub.model.properties.Config;
import org.spring.azure.hub.provider.ServiceBusConnectionProvider;
import org.spring.azure.hub.settings.processor.MessageProcessor;
import org.spring.azure.hub.model.dto.NameData;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j @RequiredArgsConstructor @Component
public class MessageBusClient {

    private final ServiceBusSharedResources sharedResources;
    private final MessageProcessor messageProcessor;
    private final ServiceBusConnectionProvider clientProvider;

    public void configureMessageClient(NameData nameData,
                                       Config config,
                                       String connectionString,
                                       ListenerMethod listener,
                                       ErrorHandlerMethod errorHandler,
                                       boolean isDeadLetter) {
        String name = nameData.getName();
        if(isDeadLetter)
            name = name + Constants.DEAD_LETTER;
        log.info("Configuring Service Bus processor for '{}'", name);
        sharedResources.getProcessorClients().put(name,
                config.getEnableSession() ?
                        withSessionProcessor(nameData, connectionString, config, listener, errorHandler, isDeadLetter)
                        : processor(nameData, connectionString, config, listener, errorHandler, isDeadLetter));
        log.info("Successfully registered processor for '{}', dead letter processor: '{}'", name, isDeadLetter);
    }

    protected ServiceBusProcessorClient processor(NameData nameData,
                                                  String connectionString,
                                                  Config config,
                                                  ListenerMethod listener,
                                                  ErrorHandlerMethod errorHandler,
                                                  boolean isDeadLetter) {
        String name = nameData.getName();
        boolean isConditional = CompletionMode.CONDITIONAL == CompletionMode.valueOf(config.getAutoComplete());
        ServiceBusClientBuilder.ServiceBusProcessorClientBuilder processorBuilder = clientProvider.getServiceBusClientBuilder(name, connectionString)
                .processor()
                .topicName(nameData.getTopic())
                .subscriptionName(nameData.getSubscription())
                .queueName(nameData.getQueue())
                .subQueue(isDeadLetter ? SubQueue.DEAD_LETTER_QUEUE : SubQueue.NONE)
                .receiveMode(ServiceBusReceiveMode.valueOf(config.getReceiveMode()))
                .maxConcurrentCalls(config.getMaxConcurrentCalls())
                .maxAutoLockRenewDuration(Duration.ofSeconds(config.getMaxAutoLockRenewDuration()))
                .prefetchCount(config.getPrefetchCount())
                .processMessage(messageContext -> messageProcessor.processMessage(name, listener, messageContext, isConditional, config.getAbandonOnSerializationError()))
                .processError(errorContext -> messageProcessor.handleError(errorHandler, errorContext));
        if (isConditional)
            processorBuilder.disableAutoComplete();
        return processorBuilder.buildProcessorClient();
    }

    protected ServiceBusProcessorClient withSessionProcessor(NameData nameData,
                                                             String connectionString,
                                                             Config config,
                                                             ListenerMethod listener,
                                                             ErrorHandlerMethod errorHandler,
                                                             boolean isDeadLetter) {
        String name = nameData.getName();
        boolean isConditional = CompletionMode.CONDITIONAL == CompletionMode.valueOf(config.getAutoComplete());
        ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder processorBuilder = clientProvider.getServiceBusClientBuilder(name, connectionString)
                .sessionProcessor()
                .topicName(nameData.getTopic())
                .subscriptionName(nameData.getSubscription())
                .queueName(nameData.getQueue())
                .subQueue(isDeadLetter ? SubQueue.DEAD_LETTER_QUEUE : SubQueue.NONE)
                .receiveMode(ServiceBusReceiveMode.valueOf(config.getReceiveMode()))
                .maxConcurrentCalls(config.getMaxConcurrentCalls())
                .maxAutoLockRenewDuration(Duration.ofSeconds(config.getMaxAutoLockRenewDuration()))
                .prefetchCount(config.getPrefetchCount())
                .processMessage(messageContext -> messageProcessor.processMessage(name, listener, messageContext, isConditional, config.getAbandonOnSerializationError()))
                .processError(errorContext -> messageProcessor.handleError(errorHandler, errorContext));
        if (CompletionMode.CONDITIONAL == CompletionMode.valueOf(config.getAutoComplete()))
            processorBuilder.disableAutoComplete();
        return processorBuilder.buildProcessorClient();
    }
}