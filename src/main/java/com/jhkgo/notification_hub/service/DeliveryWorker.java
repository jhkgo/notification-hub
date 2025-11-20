package com.jhkgo.notification_hub.service;

import com.jhkgo.notification_hub.domain.entity.NotificationDelivery;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.jhkgo.notification_hub.config.DeliveryWorkerProperties;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DeliveryWorker {

    private final DeliveryProcessingService deliveryProcessingService;
    private final DeliveryWorkerProperties properties;

    @Scheduled(fixedDelayString = "${delivery.worker.fixed-delay-ms}")
    public void processPendingDeliveries() {
        List<NotificationDelivery> deliveries = deliveryProcessingService.fetchAndLockPendingDeliveries(properties.maxBatchSize());
        if (deliveries.isEmpty()) {
            return;
        }
        deliveryProcessingService.executeDeliveries(deliveries);
    }
}
