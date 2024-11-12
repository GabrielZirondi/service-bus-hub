package org.spring.azure.hub.settings;

import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.spring.azure.hub.model.context.ServiceBusSharedResources;
import org.spring.azure.hub.model.dto.ErrorHandlerMethod;
import org.spring.azure.hub.model.dto.ListenerMethod;
import org.spring.azure.hub.model.properties.Config;
import org.spring.azure.hub.model.properties.ServiceBusProperties;
import org.spring.azure.hub.utilities.ConnectionStringUtil;
import org.spring.azure.hub.settings.client.MessageBusClient;
import org.spring.azure.hub.model.dto.NameData;

import java.util.Optional;

@Slf4j @RequiredArgsConstructor  @SuperBuilder
public abstract class AbstractMessagingSettings<ENTITY extends Config, PARENT> {

    protected final ServiceBusProperties serviceBusProperties;
    protected final ServiceBusSharedResources sharedResources;
    protected final MessageBusClient messageBusClient;

    public void initializeEntityConfiguration(ENTITY entityConfig, PARENT parentConfig) {
        setupMainEntityConfiguration(entityConfig, parentConfig);
        setupDeadLetterEntityConfiguration(entityConfig, parentConfig);
    }

    private void setupMainEntityConfiguration(ENTITY entityConfig, PARENT parentConfig) {
        String entityName = getEntityName(entityConfig, parentConfig);
        log.info("Configuring {} '{}'", getEntityType(), entityName);
        ListenerMethod listener = getListener(entityConfig, parentConfig);
        ErrorHandlerMethod errorHandler = getErrorHandler(entityConfig, parentConfig);
        validate(listener, errorHandler, entityName);
        configureListener(entityConfig, parentConfig, listener, errorHandler, false);
    }

    private void setupDeadLetterEntityConfiguration(ENTITY entityConfig, PARENT parentConfig) {
        if (Boolean.FALSE.equals(entityConfig.getEnableDeadLettering())) return;
        String deadLetterEntityName = getDeadLetterName(entityConfig, parentConfig);
        log.debug("Configuring dead-letter {} '{}'", getEntityType(), deadLetterEntityName);
        ListenerMethod deadLetterListener = getDeadLetterListener(entityConfig, parentConfig);
        ErrorHandlerMethod deadLetterErrorHandler = getDeadLetterErrorHandler(entityConfig, parentConfig);
        validate(deadLetterListener, deadLetterErrorHandler, deadLetterEntityName);
        configureListener(entityConfig, parentConfig, deadLetterListener, deadLetterErrorHandler, true);
    }

    protected void configureListener(ENTITY entityConfig, PARENT parentConfig, ListenerMethod listener, ErrorHandlerMethod errorHandler, boolean isDeadLetter) {
        String entityName = getEntityName(entityConfig, parentConfig);
        String connectionString = getConnectionString(entityConfig, parentConfig, entityName);
        NameData nameData = createNameData(entityConfig, parentConfig);
        messageBusClient.configureMessageClient(nameData, entityConfig, connectionString, listener, errorHandler, isDeadLetter);
    }

    protected String getConnectionString(ENTITY entityConfig, PARENT parentConfig, String entityName) {
        String connectionString = Optional.ofNullable(getConnectionString(entityConfig, parentConfig))
                .filter(str -> !str.isEmpty())
                .orElseGet(() ->
                        Optional.ofNullable(serviceBusProperties.getConnectionString())
                        .orElseThrow(() -> new IllegalStateException("Connection string is not specified for " + entityName)));
        return ConnectionStringUtil.ensureEntityPathInConnectionString(
                connectionString, getEntityPath(entityConfig, parentConfig), serviceBusProperties.getConnectionString());
    }

    protected void validate(ListenerMethod listener, ErrorHandlerMethod errorHandler, String entityName) {
        if (listener == null || errorHandler == null)
            throw new IllegalStateException(getEntityType() + " missing listener or error handler: " + entityName);
    }

    protected abstract String getEntityName(ENTITY entityConfig, PARENT parentConfig);

    protected abstract String getEntityType();

    protected abstract String getDeadLetterName(ENTITY entityConfig, PARENT parentConfig);

    protected abstract ListenerMethod getListener(ENTITY entityConfig, PARENT parentConfig);

    protected abstract ErrorHandlerMethod getErrorHandler(ENTITY entityConfig, PARENT parentConfig);

    protected abstract ListenerMethod getDeadLetterListener(ENTITY entityConfig, PARENT parentConfig);

    protected abstract ErrorHandlerMethod getDeadLetterErrorHandler(ENTITY entityConfig, PARENT parentConfig);

    protected abstract NameData createNameData(ENTITY entityConfig, PARENT parentConfig);

    protected abstract String getConnectionString(ENTITY entityConfig, PARENT parentConfig);

    protected abstract String getEntityPath(ENTITY entityConfig, PARENT parentConfig);
}
