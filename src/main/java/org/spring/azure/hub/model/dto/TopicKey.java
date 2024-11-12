package org.spring.azure.hub.model.dto;

import lombok.Data;

public @Data class TopicKey {

    private final String topic;
    private final String subscription;

    @Override
    public String toString() {
        return "Topic: '" + topic + '\'' +
                ", Subscription: '" + subscription + '\'';
    }
}