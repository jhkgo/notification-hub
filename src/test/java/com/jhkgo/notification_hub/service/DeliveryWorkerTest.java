package com.jhkgo.notification_hub.service;

import com.jhkgo.notification_hub.config.DeliveryWorkerProperties;
import com.jhkgo.notification_hub.domain.entity.NotificationDelivery;
import com.jhkgo.notification_hub.domain.enums.DeliveryChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeliveryWorkerTest {

    @Mock
    private DeliveryProcessingService deliveryProcessingService;

    @InjectMocks
    private DeliveryWorker deliveryWorker;

    @BeforeEach
    void setUp() {
        DeliveryWorkerProperties properties = new DeliveryWorkerProperties(10, 5000);
        deliveryWorker = new DeliveryWorker(deliveryProcessingService, properties);
    }

    @DisplayName("@Scheduled 워커가 PENDING Delivery를 가져와 실행기로 전달한다")
    @Test
    void shouldFetchAndExecutePendingDeliveriesOnSchedule() {
        NotificationDelivery delivery = new NotificationDelivery(DeliveryChannel.EMAIL, "user@example.com");
        List<NotificationDelivery> deliveries = List.of(delivery);

        when(deliveryProcessingService.fetchAndLockPendingDeliveries(10)).thenReturn(deliveries);

        deliveryWorker.processPendingDeliveries();

        verify(deliveryProcessingService).fetchAndLockPendingDeliveries(10);
        verify(deliveryProcessingService).executeDeliveries(deliveries);
    }

    @DisplayName("PENDING Delivery가 없으면 실행기를 호출하지 않는다")
    @Test
    void shouldNotExecuteWhenNoDeliveries() {
        when(deliveryProcessingService.fetchAndLockPendingDeliveries(10)).thenReturn(List.of());

        deliveryWorker.processPendingDeliveries();

        verify(deliveryProcessingService).fetchAndLockPendingDeliveries(10);
        verify(deliveryProcessingService, never()).executeDeliveries(anyList());
    }
}
