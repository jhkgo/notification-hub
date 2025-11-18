package com.jhkgo.notification_hub.controller;

import com.jhkgo.notification_hub.domain.entity.Notification;
import com.jhkgo.notification_hub.domain.entity.NotificationDelivery;
import com.jhkgo.notification_hub.domain.enums.DeliveryChannel;
import com.jhkgo.notification_hub.domain.enums.NotificationType;
import com.jhkgo.notification_hub.domain.enums.DeliveryStatus;
import com.jhkgo.notification_hub.repository.NotificationDeliveryRepository;
import com.jhkgo.notification_hub.repository.NotificationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
    void shouldStoreNotificationAndDeliveries() throws Exception {
        String payload = """
            {
              "type": "ORDER_COMPLETED",
              "title": "주문이 완료되었습니다",
              "message": "주문 번호 1234가 완료되었습니다",
              "recipientId": "customer-1",
              "metadata": {
                "orderId": "1234"
              },
              "deliveries": [
                {
                  "channel": "EMAIL",
                  "recipient": "customer@example.com"
                },
                {
                  "channel": "SLACK",
                  "recipient": "https://hooks.slack.com/services/T000/B000/XXXX"
                }
              ]
            }
            """;

        mockMvc.perform(
                post("/notifications")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload)
            )
            .andExpect(status().isAccepted());

        List<Notification> notifications = notificationRepository.findAll();
        assertThat(notifications)
            .hasSize(1)
            .first()
            .satisfies(notification -> {
                assertThat(notification.getType()).isEqualTo(NotificationType.ORDER_COMPLETED);
                assertThat(notification.getTitle()).isEqualTo("주문이 완료되었습니다");
                assertThat(notification.getDeliveries()).hasSize(2);
            });

        List<NotificationDelivery> deliveries = notificationDeliveryRepository.findAll();
        assertThat(deliveries).hasSize(2);
        assertThat(deliveries.stream().map(NotificationDelivery::getChannel))
            .containsExactlyInAnyOrder(DeliveryChannel.EMAIL, DeliveryChannel.SLACK);
    }

}
