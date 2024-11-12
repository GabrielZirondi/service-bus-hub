package org.spring.azure.hub.provider;

import com.azure.messaging.servicebus.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

public @Configuration @Slf4j class ServiceBusClientSettings {

    @ConditionalOnMissingBean(name = "serviceBusClientBuilderConfigurations")
    public @Bean Map<String, ServiceBusClientBuilder> serviceBusClientBuilderConfigurations() {
        log.debug("Initializing a new HashMap for Service Bus client builder configurations.");
        return new HashMap<>();
    }
}

//    create a bean to be used by users that want a sender with transaction or custom properties
//    how to create
//    @Configuration class Config {
//        public @Bean ServiceBusSenderClient queueClient(Map<String, ServiceBusClientBuilder> serviceBusSenderClients) {
//            ServiceBusClientBuilder queueBuilder = serviceBusSenderClients.get("queue");
//            return queueBuilder.sender().queueName("queue").buildClient();
//        }
//
//        public @Bean ServiceBusSenderClient topicClient(Map<String, ServiceBusClientBuilder> serviceBusSenderClients) {
//            ServiceBusClientBuilder topicBuilder = serviceBusSenderClients.get("topic:subscription");
//            return topicBuilder.sender().queueName("topic:subscription").buildClient();
//        }
//    }
//   how to use
//   @Component
//    class Processor{
//       private @Autowired ServiceBusSenderClient queueClient;
//
//       @QueueListener("blob-safe-d")
//       public void howToUse(MalwareScanResult result, ServiceBusReceivedMessageContext context) {
//           ServiceBusTransactionContext transaction = queueClient.createTransaction();
//           CompleteOptions transactionOptions = new CompleteOptions().setTransactionContext(transaction);
//
//           ServiceBusMessage newMessage = new ServiceBusMessage("Processed: " + result.toString());
//
//           queueClient.sendMessage(newMessage, transaction);
//           context.complete(transactionOptions);
//       }
//   }