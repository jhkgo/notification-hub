package com.jhkgo.notification_hub.service;

import com.jhkgo.notification_hub.domain.entity.NotificationDelivery;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DeliveryWorker {

    static final int FETCH_LIMIT = 10;

    private final DeliveryProcessingService deliveryProcessingService;

    @Scheduled(fixedDelayString = "${delivery.worker.fixed-delay-ms:5000}")
    public void processPendingDeliveries() {
        List<NotificationDelivery> deliveries = deliveryProcessingService.fetchAndLockPendingDeliveries(FETCH_LIMIT);
        if (deliveries.isEmpty()) {
            return;
        }
        deliveryProcessingService.executeDeliveries(deliveries);
    }
}
