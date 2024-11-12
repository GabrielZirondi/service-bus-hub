package org.spring.azure.hub.model.dto;

import lombok.Builder;
import lombok.Data;

public @Data @Builder class NameData {

    private String queue;
    private String topic;
    private String subscription;

    public boolean isTopic() {
        return this.topic != null;
    }

    public String getName() {
        return isTopic() ? topic + "/" + subscription : queue;
    }
}