package com.jhkgo.notification_hub.service;

import com.jhkgo.notification_hub.domain.entity.NotificationDelivery;
import com.jhkgo.notification_hub.domain.enums.DeliveryStatus;
import com.jhkgo.notification_hub.repository.NotificationDeliveryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryProcessingService {

    private final NotificationDeliveryRepository notificationDeliveryRepository;
    private final ChannelDeliveryExecutor channelDeliveryExecutor;

    @Transactional
    public List<NotificationDelivery> fetchAndLockPendingDeliveries(int limit) {
        List<NotificationDelivery> pendingDeliveries = notificationDeliveryRepository
            .findByStatus(DeliveryStatus.PENDING)
            .stream()
            .limit(limit)
            .toList();

        List<Long> ids = pendingDeliveries.stream()
            .map(NotificationDelivery::getId)
            .toList();

        if (!ids.isEmpty()) {
            notificationDeliveryRepository.updateStatusIn(ids, DeliveryStatus.PROCESSING);
            pendingDeliveries.forEach(delivery -> delivery.markProcessing());
        }

        return pendingDeliveries;
    }

    @Transactional
    public void executeDeliveries(List<NotificationDelivery> deliveries) {
        deliveries.forEach(delivery -> {
            DeliveryExecutionResult result = channelDeliveryExecutor.execute(delivery);
            if (result.succeeded()) {
                delivery.markSuccess();
            } else {
                delivery.markFailed(result.errorMessage());
            }
        });

        notificationDeliveryRepository.saveAll(deliveries);
    }
}
