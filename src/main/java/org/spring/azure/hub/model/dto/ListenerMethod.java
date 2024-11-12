package org.spring.azure.hub.model.dto;

import lombok.Data;

import java.lang.reflect.Method;

public @Data class ListenerMethod {

    private final Object bean;
    private final Method method;
    private final int order;

}