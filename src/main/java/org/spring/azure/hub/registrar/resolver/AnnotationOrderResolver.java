package org.spring.azure.hub.registrar.resolver;

import lombok.experimental.UtilityClass;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;

import java.lang.reflect.Method;

@UtilityClass
public class AnnotationOrderResolver {

    public int resolve(Method method, Object bean) {
        Order order = AnnotationUtils.findAnnotation(method, Order.class);
        if (order == null)
            order = AnnotationUtils.findAnnotation(bean.getClass(), Order.class);
        return (order != null) ? order.value() : Integer.MAX_VALUE;
    }
}