package com.jhkgo.notification_hub.service;

import com.jhkgo.notification_hub.domain.entity.Notification;
import com.jhkgo.notification_hub.domain.entity.NotificationDelivery;
import com.jhkgo.notification_hub.domain.enums.DeliveryChannel;
import com.jhkgo.notification_hub.domain.enums.NotificationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmailNotifierTest {

    @DisplayName("Email 모의 전송 시 로그 출력 후 성공을 반환한다")
    @Test
    void shouldReturnSuccessWhenEmailIsLogged() {
        EmailNotifier emailNotifier = new EmailNotifier();
        Notification notification = new Notification(
            NotificationType.ORDER_COMPLETED,
            "주문 완료",
            "주문 완료 알림",
            "customer-1",
            null
        );
        NotificationDelivery delivery = new NotificationDelivery(DeliveryChannel.EMAIL, "user@example.com");
        notification.addDelivery(delivery);

        DeliveryExecutionResult result = emailNotifier.send(delivery);

        assertThat(result.succeeded()).isTrue();
    }
}
