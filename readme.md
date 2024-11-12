
# Azure Service Bus Hub

A Spring-based framework designed to handle multiple listeners for Azure Service Bus queues and topics-subscriptions.
It provides a ready to go and yet fully customizable way for developers to manage message processing, error handling, and dead-letter queues.

## Table of Contents

- [Features](#features)
- [Getting Started](#getting-started)
    - [Prerequisites](#prerequisites)
    - [Installation](#installation)
    - [Configuration](#configuration)
- [Usage](#usage)
    - [Annotations](#annotations)
        - [@QueueListener](#queuelistener)
        - [@TopicListener](#topiclistener)
        - [@ErrorHandler](#errorhandler)
        - [@GlobalErrorHandler](#globalerrorhandler)
        - [@GlobalDeadLetterHandler](#globaldeadletterhandler)
    - [Defining Listeners](#defining-listeners)
    - [Handling Errors](#handling-errors)
    - [Dead Letter Queues](#dead-letter-queues)
- [Property Hierarchy](#property-hierarchy)
- [Available Properties](#available-properties)
  - [Service Bus Properties](#service-bus-properties)
  - [Queue Properties](#queue-properties)
  - [Topic Properties](#topic-properties)
  - [Subscription Properties](#subscription-properties)
- [Completion Mode](#completion-mode)
- [Examples](#examples)
- [License](#license)

## Features

- **Multiple Listener Support**: Easily manage multiple listeners for different queues and topics.
- **Customizable Error Handling**: Use global or entity-specific error handlers to manage exceptions gracefully.
- **Dead Letter Queue Management**: Automatically handle messages that cannot be processed.
- **Hierarchical Configuration**: Inherit and override configurations at various levels (Service Bus, Topic/Queue, Subscription).
- **Flexible Serialization**: Register custom serializers for message processing.
- **Seamless Integration**: Designed to integrate smoothly with Spring Boot applications.

## Getting Started

### Prerequisites

- **Java 8 or higher**
- **Spring Boot 2.7**
- **Azure Service Bus Namespace**
- **Maven or Gradle Build Tool**

### Installation

Add the framework dependency to your project.

**Maven:**
```xml
<dependency>
    <groupId>io.github.springframework-azure</groupId>
    <artifactId>service-bus-hub</artifactId>
    <version>1.0.0</version>
</dependency>
```

**Gradle:**
```groovy
implementation 'org.spring.azure:servicebus-spring-boot-starter:1.0.0'
```

You can find a updated version at: [Sonatype](https://central.sonatype.com/artifact/io.github.springframework-azure/service-bus-hub)
### Configuration

Configure the framework using `application.yml` or `application.properties`.

To get started, all you need is a connection string to your Azure Service Bus namespace:
**Example `application.yml`:**
```yaml
servicebus.connectionString: Endpoint=sb://your-servicebus.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=yourkey
```

You can also configure multiple connection strings for different entities (queues, topics, subscriptions) to connect to multiple namespaces or to apply distinct settings per entity.
**Example `application.yml`:**
```yaml
servicebus:
  connectionString: Endpoint=sb://your-servicebus.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=yourkey
  maxConcurrentCalls: 1
  enableCrossEntityTransactions: true
  prefetchCount: 10
  queues:
    - name: secure
      connectionString: Endpoint=sb://your-servicebus.servicebus.windows.net/;SharedAccessKeyName=this;SharedAccessKey=anotherkey;EntityPath=secure;
      maxConcurrentCalls: 2
  topics:
    - name: malware-detected
      maxConcurrentCalls: 4
      enableCrossEntityTransactions: false
      connectionString: Endpoint=sb://your-servicebus.servicebus.windows.net/;SharedAccessKeyName=this;SharedAccessKey=anotherkey;EntityPath=malware-detected
      subscriptions:
        - name: on-cloud
          maxConcurrentCalls: 3
          maxAutoLockRenewDuration: 60
```

## Usage

### Annotations

The framework provides a annotation based way to define listeners and error handlers.

#### @QueueListener

Registers a method as a listener for a specific Service Bus queue.

```java
@QueueListener("secure")
public void processSecureMessage(MalwareScanResult result) {
    log.info("Processing MalwareScanResult: {}", result);
}
```

**Parameters:**

- `value`: The name of the queue to listen to.
- `isDeadLetterQueue` (optional): Set to `true` to listen to the dead-letter queue.

#### @TopicListener

Registers a method as a listener for a specific Service Bus topic and subscription.

```java
@TopicListener(topic = "malware-detected", subscription = "on-cloud", isDeadLetterQueue = true)
public void processMalwareDetectedMessageDLQ(MalwareScanResult result) {
    log.info("Processing MalwareScanResult: {}", result);
}
```

**Parameters:**

- `topic`: The name of the topic.
- `subscription`: The name of the subscription.
- `isDeadLetterQueue` (optional): Set to `true` to listen to the dead-letter queue.

#### @ErrorHandler

Defines a method to handle errors for a specific queue or topic subscription.

```java
@ErrorHandler(queue = "secure")
public void handleSecureQueueError(ServiceBusErrorContext context) {
    log.error("Error processing message from secure queue: {}", context.getException().getMessage());
}
```

**Parameters:**

- `queue` (optional): The name of the queue.
- `topic` and `subscription` (optional): The name of the topic and subscription.

**Note:** Either `queue` or both `topic` and `subscription` must be specified.

#### @GlobalErrorHandler

Defines a global method to handle errors across all queues and topics.

```java
@GlobalErrorHandler
public void handleGlobalError(ServiceBusErrorContext context) {
    log.error("Global error handler caught an exception: {}", context.getException().getMessage());
}
```

#### @GlobalDeadLetterHandler

Defines a global method to handle messages from dead-letter queues when no specific handler is defined.

```java
@GlobalDeadLetterHandler
public void handleGlobalDeadLetter(ServiceBusErrorContext context) {
    log.warn("Global dead-letter handler processed a message: {}", context.getMessage().getBody().toString());
}
```

## Defining Listeners

To start listening to queues and topics, simply annotate your methods with `@QueueListener` or `@TopicListener` and provide the necessary parameters.

**Example:**
```java
@Component
public class MyServiceBusListener {

    @QueueListener("secure")
    public void processSecureMessage(MalwareScanResult result) {
        log.info("Processing MalwareScanResult: {}", result);
    }

    @TopicListener(topic = "malware-detected", subscription = "on-cloud")
    public void processMalwareDetectedMessage(MalwareScanResult result) {
        log.info("Processing MalwareScanResult: {}", result);
    }

    @TopicListener(topic = "malware-detected", subscription = "on-cloud", isDeadLetterQueue = true)
    public void processMalwareDetectedMessageDLQ(MalwareScanResult result) {
        log.info("Processing MalwareScanResult from DLQ: {}", result);
    }
}
```

## Handling Errors
Use @ErrorHandler and @GlobalErrorHandler to define methods that handle exceptions occurring during message processing.
**Example:**
```java

@Component
public class ErrorHandlers {

    @ErrorHandler(queue = "secure")
    public void handleSecureQueueError(ServiceBusErrorContext context) {
        log.error("Error processing message from secure queue: {}", context.getException().getMessage());
    }

    @GlobalErrorHandler
    public void handleGlobalError(ServiceBusErrorContext context) {
        log.error("Global error handler caught an exception: {}", context.getException().getMessage());
    }
}
```
Error Handling in Listeners: You can handle errors directly within your listener method using a try-catch block. However, be aware that this only captures errors occurring during method execution. Errors happening before the listener is invoked, such as serialization issues, won't be caught here. For those, use an @ErrorHandler specific to the entity or a @GlobalErrorHandler for all such errors.

Leverage Global Handlers: Use @GlobalErrorHandler and @GlobalDeadLetterHandler for common error scenarios that may apply across multiple entities. Global handlers simplify error management by centralizing handling logic for widespread or repetitive issues.

Use Specific Error Handlers: When specific queues or topics need distinct error handling, define an @ErrorHandler for those entities. Specific error handlers ensure that any unique requirements for a particular entity are met directly.

## Dead Letter Queues
Dead-letter queues are automatically associated with each queue and subscription. To listen to dead-letter queues, create a new listener and set the isDeadLetterQueue parameter to true.
**Example:**

```java
@QueueListener(value = "secure", isDeadLetterQueue = true)
public void processSecureDeadLetterMessage(MalwareScanResult result) {
    log.info("Processing MalwareScanResult from secure DLQ: {}", result);
}

@TopicListener(topic = "malware-detected", subscription = "on-cloud", isDeadLetterQueue = true)
public void processMalwareDetectedMessageDLQ(MalwareScanResult result) {
    log.info("Processing MalwareScanResult from malware-detected DLQ: {}", result);
}
```

## Property Hierarchy

Configuration properties follow a hierarchy where more specific settings override general ones:
1. **Service Bus Level**: Global defaults for all entities.
2. **Queue/Topic Level**: Specific settings for individual queues or topics that override global settings.
3. **Subscription Level**: Overrides both global and topic-level settings for specific subscriptions within topics.

## Available Properties

The following properties can be configured at any level (Service Bus, Queue, Topic, or Subscription). When set at a more specific level (like a queue or subscription), they override values set at a more general level.

| Property                        | Type      | Description                                                                                                              |
|---------------------------------|-----------|--------------------------------------------------------------------------------------------------------------------------|
| `receiveMode`                   | `String`  | How messages are received: `PEEK_LOCK` (default) or `RECEIVE_AND_DELETE`.                                                |
| `autoComplete`                  | `String`  | Controls if messages are automatically marked complete. Default is `CONDITIONAL`, meaning completion depends on success. |
| `enableCrossEntityTransactions` | `Boolean` | Enables transactions across multiple entities. Default is `false`.                                                       |
| `enableSession`                 | `Boolean` | Enables session support if needed by entities. Default is `false`.                                                       |
| `maxAutoLockRenewDuration`      | `Integer` | Maximum duration (in seconds) to renew message locks. Default is `300` seconds.                                          |
| `maxConcurrentCalls`            | `Integer` | Maximum concurrent calls for message processing. Default is `1`.                                                         |
| `prefetchCount`                 | `Integer` | Number of messages to prefetch for performance optimization. Default is `0` (no prefetch).                               |
| `enableDeadLettering`           | `Boolean` | Enables dead-lettering for messages that cannot be processed. Default is `true`.                                         |
| `abandonOnSerializationError`   | `Boolean` | Determines if messages should be abandoned upon serialization errors. Defaults is `true`.                                |
---

In addition to the shared properties above, each entity type (Service Bus, Queue, Topic, Subscription) has specific attributes:

### Service Bus Properties

| Property           | Type                 | Description                                                                                       |
|--------------------|----------------------|---------------------------------------------------------------------------------------------------|
| `connectionString` | `String`             | Connection string for the Azure Service Bus namespace. Required for Service Bus access.           |
| `queues`           | `List<TopicConfig>`  | List of subscription configurations for this topic, allowing individual settings for each one.    |
| `topics`           | `List<QueueConfig>`  | List of subscription configurations for this topic, allowing individual settings for each one.    |

### Queue Properties

| Property           | Type     | Description                                                                                          |
|--------------------|----------|------------------------------------------------------------------------------------------------------|
| `name`             | `String` | The name of the queue. Required for defining queue-specific configurations.                          |
| `connectionString` | `String` | Optional connection string specific to this queue, overriding the global connection if provided.     |

### Topic Properties

| Property           | Type                        | Description                                                                                       |
|--------------------|-----------------------------|---------------------------------------------------------------------------------------------------|
| `name`             | `String`                    | The name of the topic. Required for defining topic-specific configurations.                       |
| `connectionString` | `String`                    | Optional connection string specific to this topic, overriding the global connection if provided.  |
| `subscriptions`    | `List<SubscriptionConfig>`  | List of subscription configurations for this topic, allowing individual settings for each one.    |

### Subscription Properties

| Property           | Type     | Description                                                                                      |
|--------------------|----------|--------------------------------------------------------------------------------------------------|
| `name`             | `String` | The name of the subscription. Required for defining subscription-specific configurations.        |
---

## Completion Mode

1. **CONDITIONAL** (default)
2. **AUTO**

- **CONDITIONAL**:
    - **Listener without `ServiceBusReceivedMessageContext`**:
        - Message completion is automatic if no exception is thrown.
        - If an exception is thrown, the message will be abandoned, meaning the lock will be released, and the message will be immediatly available for re-consumption.
    - **Listener with `ServiceBusReceivedMessageContext`**:
        - The user is responsible for manually completing or abandoning the message by calling `context.complete()` or `context.abandon()`.
        - Messages that encounter errors before the method call during serialization will remain locked or be abandoned based on the value of the property `abandonOnSerializationError`. By default, this property is `true`, meaning messages with serialization errors will be released immediately unless the user overrides this setting for the desired entity.

- **AUTO**:
    - Message completion and abandonment are handled automatically by the Microsoft SDK.
    - If an exception is thrown in the listener method, the message will be abandoned according to SDK rules, releasing the lock and making it available for re-consumption.

### Examples

The type of message acknowledgment is determined by the presence of the `ServiceBusReceivedMessageContext` parameter in the listener method signature.

- **Automatic Completion Example**:

  In this example, since the listener method has only a single parameter (`MalwareScanResult`), the framework handles message completion automatically when the method exits without an exception.

  ```java
  @TopicListener(topic = "malware-detected", subscription = "on-cloud")
  public void processMalwareDetectedMessage(MalwareScanResult result) {
      log.info("Processing MalwareScanResult: {}", result);
  }

- **Manual Completion Example (using ServiceBusReceivedMessageContext)**:

When the listener method includes a ServiceBusReceivedMessageContext parameter, the user is responsible for message completion and abandonment. In the example below, context.complete() is called upon successful processing, while context.abandon() is used in case of an error, releasing the lock on the message.

```java
@TopicListener(topic = "malware-detected", subscription = "on-cloud", isDeadLetterQueue = true)
public void processMalwareDetectedMessageDLQ(MalwareScanResult result, ServiceBusReceivedMessageContext context) {
    try {
        log.info("Processing MalwareScanResult: {}", result);
        context.complete();
    } catch (Throwable e) {
        context.abandon();  // Removing lock to make the message available for reprocessing
    }
}
  ```

### Wrapping Up
- **Use `ServiceBusReceivedMessageContext` for Fine-Grained Control**: Include `ServiceBusReceivedMessageContext` in your listener method when you need precise control over message acknowledgment. This is especially helpful when implementing custom error handling logic that requires manual control over completion or abandonment of messages.
- **Encapsulate Errors with `try-catch` for Complete Control**: By wrapping your listener code in a `try-catch` block and handling all exceptions internally, you can prevent errors from propagating back to the framework. This approach allows you to decide exactly when to complete or abandon messages, giving you full control over message flow without relying on default handling.
- **Ensure Proper Use of `context.complete()` and `context.abandon()`**: When using `ServiceBusReceivedMessageContext`, make sure your code calls `context.complete()` on successful processing and `context.abandon()` in case of errors. This ensures that each message is either properly acknowledged or made available for re-processing, depending on the outcome of your custom logic.

## Example
For a complete example project, visit the [Azure Service Bus Hub Example](https://github.com/GabrielZirondi/service-bus-hub-example?tab=readme-ov-file) repository, which demonstrates various configurations and listener setups.

## License
This project is licensed under the Apache License 2.0, allowing you to freely use it for commercial purposes. See the full license text [here](LICENSE).