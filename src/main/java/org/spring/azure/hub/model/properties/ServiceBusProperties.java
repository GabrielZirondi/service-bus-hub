package org.spring.azure.hub.model.properties;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Configuration @ConfigurationProperties(prefix = "servicebus")
@Data @EqualsAndHashCode(callSuper = true) @AllArgsConstructor @NoArgsConstructor
public class ServiceBusProperties extends Config {
    private String connectionString;
    private List<QueueConfig> queues = new ArrayList<>();
    private List<TopicConfig> topics = new ArrayList<>();

    @PostConstruct
    public void init() {
        this.inheritPropertiesFrom(null);
        for (QueueConfig queue : queues)
            queue.inheritPropertiesFrom(this);
        for (TopicConfig topic : topics) {
            topic.inheritPropertiesFrom(this);
            for (TopicConfig.SubscriptionConfig subscription : topic.getSubscriptions())
                subscription.inheritPropertiesFrom(topic);
        }
    }

    @Data @EqualsAndHashCode(callSuper = true) @Builder @AllArgsConstructor @NoArgsConstructor
    public static class QueueConfig extends Config {
        private String name;
        private String connectionString;
    }

    @Data @EqualsAndHashCode(callSuper = true) @Builder @AllArgsConstructor @NoArgsConstructor
    public static class TopicConfig extends Config {
        private String name;
        private String connectionString;
        private List<SubscriptionConfig> subscriptions = new ArrayList<>();

        @Data @EqualsAndHashCode(callSuper = true) @Builder @AllArgsConstructor @NoArgsConstructor
        public static class SubscriptionConfig extends Config {
            private String name;
        }
    }
}