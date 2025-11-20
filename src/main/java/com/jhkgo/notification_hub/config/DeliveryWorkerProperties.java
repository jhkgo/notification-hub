package com.jhkgo.notification_hub.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "delivery.worker")
public record DeliveryWorkerProperties(
    int maxBatchSize,
    long fixedDelayMs
) {}
