package com.jhkgo.notification_hub.service;

import com.jhkgo.notification_hub.domain.entity.NotificationDelivery;
import com.jhkgo.notification_hub.domain.enums.DeliveryChannel;
import org.junit.jupiter.api.BeforeEach;
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

    private static final int FETCH_LIMIT = 10;

    @Mock
    private DeliveryProcessingService deliveryProcessingService;

    @InjectMocks
    private DeliveryWorker deliveryWorker;

    @BeforeEach
    void setUp() {
        deliveryWorker = new DeliveryWorker(deliveryProcessingService);
    }

    @Test
    void shouldFetchAndExecutePendingDeliveriesOnSchedule() {
        NotificationDelivery delivery = new NotificationDelivery(DeliveryChannel.EMAIL, "user@example.com");
        List<NotificationDelivery> deliveries = List.of(delivery);

        when(deliveryProcessingService.fetchAndLockPendingDeliveries(FETCH_LIMIT)).thenReturn(deliveries);

        deliveryWorker.processPendingDeliveries();

        verify(deliveryProcessingService).fetchAndLockPendingDeliveries(FETCH_LIMIT);
        verify(deliveryProcessingService).executeDeliveries(deliveries);
    }

    @Test
    void shouldNotExecuteWhenNoDeliveries() {
        when(deliveryProcessingService.fetchAndLockPendingDeliveries(FETCH_LIMIT)).thenReturn(List.of());

        deliveryWorker.processPendingDeliveries();

        verify(deliveryProcessingService).fetchAndLockPendingDeliveries(FETCH_LIMIT);
        verify(deliveryProcessingService, never()).executeDeliveries(anyList());
    }
}
