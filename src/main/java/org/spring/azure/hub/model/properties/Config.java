package org.spring.azure.hub.model.properties;

import lombok.Data;

import static com.azure.messaging.servicebus.models.ServiceBusReceiveMode.PEEK_LOCK;
import static org.spring.azure.hub.model.properties.CompletionMode.CONDITIONAL;

/**
 * maxAutoLockRenewDuration in seconds
 */
public @Data class Config {

    private String receiveMode;
    private String autoComplete;
    private Boolean enableCrossEntityTransactions;
    private Boolean enableSession;
    private Integer maxAutoLockRenewDuration;
    private Integer maxConcurrentCalls;
    private Integer prefetchCount;
    private Boolean enableDeadLettering;
    private Boolean abandonOnSerializationError;

    public void inheritPropertiesFrom(Config parent) {
        this.receiveMode = this.receiveMode != null ? this.receiveMode
                : parent != null && parent.getReceiveMode() != null ? parent.getReceiveMode() : PEEK_LOCK.toString();

        this.autoComplete = this.autoComplete != null ? this.autoComplete
                : parent != null && parent.getAutoComplete() != null ? parent.getAutoComplete() : CONDITIONAL.toString();

        this.enableCrossEntityTransactions = this.enableCrossEntityTransactions != null ? this.enableCrossEntityTransactions
                : parent != null && parent.enableCrossEntityTransactions != null ? parent.getEnableCrossEntityTransactions() : false;

        this.enableSession = this.enableSession != null ? this.enableSession
                : parent != null && parent.enableSession != null ? parent.enableSession : false;

        this.maxAutoLockRenewDuration = this.maxAutoLockRenewDuration != null ? this.maxAutoLockRenewDuration
                : parent != null && parent.getMaxAutoLockRenewDuration() != null ? parent.getMaxAutoLockRenewDuration() : 300;

        this.maxConcurrentCalls = this.maxConcurrentCalls != null ? this.maxConcurrentCalls
                : parent != null && parent.getMaxConcurrentCalls() != null ? parent.getMaxConcurrentCalls() : 1;

        this.prefetchCount = this.prefetchCount != null ? this.prefetchCount
                : parent != null && parent.getPrefetchCount() != null ? parent.getPrefetchCount() : 0;

        this.enableDeadLettering = this.enableDeadLettering != null ? this.enableDeadLettering
                : parent != null && parent.enableDeadLettering != null ? parent.getEnableDeadLettering() : true;

        this.abandonOnSerializationError = this.abandonOnSerializationError != null ? this.abandonOnSerializationError
                : parent != null && parent.abandonOnSerializationError != null ? parent.getAbandonOnSerializationError() : true;
    }
}