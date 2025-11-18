package com.jhkgo.notification_hub.service;

import com.jhkgo.notification_hub.domain.entity.Notification;
import com.jhkgo.notification_hub.domain.entity.NotificationDelivery;
import com.jhkgo.notification_hub.domain.enums.DeliveryChannel;
import com.jhkgo.notification_hub.domain.enums.DeliveryStatus;
import com.jhkgo.notification_hub.domain.enums.NotificationType;
import com.jhkgo.notification_hub.repository.NotificationDeliveryRepository;
import com.jhkgo.notification_hub.repository.NotificationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class DeliveryProcessingServiceTest {

    @Autowired
    private DeliveryProcessingService deliveryProcessingService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationDeliveryRepository notificationDeliveryRepository;

    @BeforeEach
    void setUp() {
        notificationDeliveryRepository.deleteAll();
        notificationRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        notificationDeliveryRepository.deleteAll();
        notificationRepository.deleteAll();
    }

    @Test
    @Transactional
    void shouldLockPendingDeliveriesAsProcessing() {
        Notification notification = new Notification(
            NotificationType.PAYMENT_FAILED,
            "결제 실패",
            "결제 실패 알림",
            "customer-1",
            null
        );
        NotificationDelivery pendingDelivery = new NotificationDelivery(DeliveryChannel.EMAIL, "user@example.com");
        notification.addDelivery(pendingDelivery);
        notificationRepository.save(notification);

        List<NotificationDelivery> lockedDeliveries = deliveryProcessingService.fetchAndLockPendingDeliveries(1);

        assertThat(lockedDeliveries).hasSize(1);
        assertThat(lockedDeliveries.getFirst().getStatus()).isEqualTo(DeliveryStatus.PROCESSING);
    }
}
