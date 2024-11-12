package org.spring.azure.hub.registrar.validator;

import java.lang.reflect.Method;

public class DuplicateErrorHandlerException extends IllegalStateException {

    public DuplicateErrorHandlerException(String name, Method method, Object bean) {
        super(String.format("Multiple @ErrorHandler methods found. Duplicate error handler detected for: '%s'. Method: %s in bean: %s",
                name, method.getName(), bean.getClass().getSimpleName()));
    }
}