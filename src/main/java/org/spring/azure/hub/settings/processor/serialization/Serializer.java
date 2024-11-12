
package org.spring.azure.hub.settings.processor.serialization;

import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.spring.azure.hub.model.context.ServiceBusSharedResources;

@Slf4j @UtilityClass
public class Serializer {

    public static Object serialize(Class<?> targetType,
                                   ServiceBusReceivedMessageContext message,
                                   ObjectMapper objectMapper,
                                   ServiceBusSharedResources sharedResources) {
        ServiceBusSerializer<?> serializer = sharedResources.getSerializerMap().get(targetType);
        if (serializer != null) {
            log.debug("Deserializing message using custom serializer for type '{}'", targetType.getName());
            return serializer.serialize(message);
        } else {
            try {
                log.debug("Deserializing message to type '{}'", targetType.getName());
                byte[] messageBytes = message.getMessage().getBody().toBytes();
                return objectMapper.readValue(messageBytes, targetType);
            } catch (Exception e) {
                throw new IllegalStateException(
                        String.format("Failed to deserialize message to type: '%s'. Ensure the message body is correctly formatted.", targetType.getName()),
                        e
                );
            }
        }
    }
}