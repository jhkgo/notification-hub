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
import org.junit.jupiter.api.DisplayName;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

    @DisplayName("POST /notifications 요청 시 Notification과 채널별 Delivery가 저장된다")
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

    @DisplayName("저장된 Delivery의 초기 상태가 PENDING으로 설정된다")
    @Test
    @Transactional
    void shouldStoreDeliveriesWithPendingStatus() throws Exception {
        String payload = """
            {
              "type": "PAYMENT_FAILED",
              "title": "결제에 실패했습니다",
              "message": "결제 번호 999가 실패했습니다",
              "recipientId": "customer-1",
              "deliveries": [
                {
                  "channel": "EMAIL",
                  "recipient": "user@example.com"
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

        NotificationDelivery delivery = notificationDeliveryRepository.findAll().getFirst();
        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.PENDING);
    }

    @DisplayName("Notification 목록을 기본 정보와 Delivery 요약, 페이징 정보와 함께 제공한다")
    @Test
    void shouldReturnNotificationListWithSummaryAndPagination() throws Exception {
        Notification oldNotification = new Notification(
            NotificationType.ORDER_COMPLETED,
            "주문 완료",
            "주문 번호 1234가 완료되었습니다",
            "customer-1",
            null
        );
        oldNotification.addDelivery(new NotificationDelivery(DeliveryChannel.EMAIL, "user1@example.com"));
        notificationRepository.save(oldNotification);

        Notification latestNotification = new Notification(
            NotificationType.PAYMENT_FAILED,
            "결제 실패",
            "결제 번호 999가 실패했습니다",
            "customer-2",
            null
        );
        NotificationDelivery successDelivery = new NotificationDelivery(DeliveryChannel.EMAIL, "user2@example.com");
        successDelivery.markSuccess();
        NotificationDelivery failedDelivery = new NotificationDelivery(DeliveryChannel.SLACK, "https://webhook");
        failedDelivery.markFailed("slack error");
        latestNotification.addDelivery(successDelivery);
        latestNotification.addDelivery(failedDelivery);
        notificationRepository.save(latestNotification);

        mockMvc.perform(
                get("/notifications")
                    .param("page", "0")
                    .param("size", "1")
                    .param("sort", "id,desc")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(latestNotification.getId()))
            .andExpect(jsonPath("$.content[0].type").value("PAYMENT_FAILED"))
            .andExpect(jsonPath("$.content[0].title").value("결제 실패"))
            .andExpect(jsonPath("$.content[0].recipientId").value("customer-2"))
            .andExpect(jsonPath("$.content[0].deliverySummary.totalCount").value(2))
            .andExpect(jsonPath("$.content[0].deliverySummary.succeedCount").value(1))
            .andExpect(jsonPath("$.content[0].deliverySummary.failedCount").value(1))
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(1))
            .andExpect(jsonPath("$.totalElements").value(2))
            .andExpect(jsonPath("$.totalPages").value(2));
    }

    @DisplayName("특정 Notification의 Delivery 목록을 상세 정보로 제공한다")
    @Test
    void shouldReturnDeliveryListByNotification() throws Exception {
        Notification notification = new Notification(
            NotificationType.SYSTEM_ALERT,
            "장애",
            "서비스 장애",
            "operator-1",
            null
        );
        NotificationDelivery emailDelivery = new NotificationDelivery(DeliveryChannel.EMAIL, "user@example.com");
        emailDelivery.markSuccess();
        NotificationDelivery slackDelivery = new NotificationDelivery(DeliveryChannel.SLACK, "https://webhook");
        slackDelivery.markFailed("slack error");
        notification.addDelivery(emailDelivery);
        notification.addDelivery(slackDelivery);
        notificationRepository.save(notification);

        mockMvc.perform(
                get("/notifications/{id}/deliveries", notification.getId())
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(emailDelivery.getId()))
            .andExpect(jsonPath("$[0].channel").value("EMAIL"))
            .andExpect(jsonPath("$[0].status").value("SUCCESS"))
            .andExpect(jsonPath("$[0].recipient").value("user@example.com"))
            .andExpect(jsonPath("$[0].sentAt").isNotEmpty())
            .andExpect(jsonPath("$[1].id").value(slackDelivery.getId()))
            .andExpect(jsonPath("$[1].channel").value("SLACK"))
            .andExpect(jsonPath("$[1].status").value("FAILED"))
            .andExpect(jsonPath("$[1].errorMessage").value("slack error"));
    }
}
