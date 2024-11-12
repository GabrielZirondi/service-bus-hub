package org.spring.azure.hub.provider;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j @RequiredArgsConstructor
public class ServiceBusConnectionProvider {

    protected final Map<String, ServiceBusClientBuilder> serviceBusClientBuilderConfigurations;

    public ServiceBusClientBuilder getServiceBusClientBuilder(String name, String connectionString) {
        log.debug("Retrieving ServiceBusClientBuilder for name: '{}'.", name);
        ServiceBusClientBuilder builder = serviceBusClientBuilderConfigurations.getOrDefault(name, getDefaultValue(connectionString));
        if (builder == null)
            log.warn("No ServiceBusClientBuilder found for name: '{}'. Using default configuration with the provided connection string.", name);
         else
            log.debug("ServiceBusClientBuilder successfully retrieved for name: '{}'.", name);
        return builder;
    }

    protected ServiceBusClientBuilder getDefaultValue(String connectionString) {
        return new ServiceBusClientBuilder().connectionString(connectionString);
    }
}