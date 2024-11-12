package org.spring.azure.hub.settings.processor.serialization;

import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;

public interface ServiceBusSerializer<T> {

    T serialize(ServiceBusReceivedMessageContext message);

}