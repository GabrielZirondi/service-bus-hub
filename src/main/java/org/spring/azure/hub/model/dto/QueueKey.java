package org.spring.azure.hub.model.dto;

import lombok.Data;

public @Data class QueueKey {

    private final String name;
    private final boolean isDeadLetter;

}