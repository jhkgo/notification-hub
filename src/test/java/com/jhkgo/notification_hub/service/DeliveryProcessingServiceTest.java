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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
class DeliveryProcessingServiceTest {

    @Autowired
    private DeliveryProcessingService deliveryProcessingService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationDeliveryRepository notificationDeliveryRepository;

    @MockBean
    private ChannelDeliveryExecutor channelDeliveryExecutor;

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

    @Test
    void shouldExecuteDeliveriesThroughChannelExecutor() {
        Notification notification = new Notification(
            NotificationType.SYSTEM_ALERT,
            "시스템 장애",
            "서버가 응답하지 않습니다",
            "operator-1",
            null
        );
        NotificationDelivery pendingDelivery = new NotificationDelivery(DeliveryChannel.SLACK, "https://slack-webhook");
        notification.addDelivery(pendingDelivery);
        notificationRepository.save(notification);

        List<NotificationDelivery> lockedDeliveries = deliveryProcessingService.fetchAndLockPendingDeliveries(1);
        when(channelDeliveryExecutor.execute(any(NotificationDelivery.class)))
            .thenReturn(DeliveryExecutionResult.success());
        deliveryProcessingService.executeDeliveries(lockedDeliveries);

        verify(channelDeliveryExecutor, times(1)).execute(any(NotificationDelivery.class));
    }

    @Test
    void shouldUpdateDeliveryStatusBasedOnExecutionResult() {
        Notification notification = new Notification(
            NotificationType.ORDER_COMPLETED,
            "주문 완료",
            "주문 번호 1234",
            "customer-1",
            null
        );
        NotificationDelivery delivery = new NotificationDelivery(DeliveryChannel.EMAIL, "user@example.com");
        notification.addDelivery(delivery);
        notificationRepository.save(notification);

        List<NotificationDelivery> lockedDeliveries = deliveryProcessingService.fetchAndLockPendingDeliveries(1);

        when(channelDeliveryExecutor.execute(lockedDeliveries.getFirst()))
            .thenReturn(DeliveryExecutionResult.success());

        deliveryProcessingService.executeDeliveries(lockedDeliveries);

        NotificationDelivery updatedDelivery = notificationDeliveryRepository.findAll().getFirst();
        assertThat(updatedDelivery.getStatus()).isEqualTo(DeliveryStatus.SUCCESS);
    }

    @Test
    void shouldMarkDeliveryFailedWhenExecutionFails() {
        Notification notification = new Notification(
            NotificationType.SYSTEM_ALERT,
            "장애",
            "서비스 장애",
            "operator-1",
            null
        );
        NotificationDelivery delivery = new NotificationDelivery(DeliveryChannel.SLACK, "https://webhook");
        notification.addDelivery(delivery);
        notificationRepository.save(notification);

        List<NotificationDelivery> lockedDeliveries = deliveryProcessingService.fetchAndLockPendingDeliveries(1);

        when(channelDeliveryExecutor.execute(lockedDeliveries.getFirst()))
            .thenReturn(DeliveryExecutionResult.failure("slack error"));

        deliveryProcessingService.executeDeliveries(lockedDeliveries);

        NotificationDelivery updatedDelivery = notificationDeliveryRepository.findAll().getFirst();
        assertThat(updatedDelivery.getStatus()).isEqualTo(DeliveryStatus.FAILED);
        assertThat(updatedDelivery.getErrorMessage()).isEqualTo("slack error");
    }
}
