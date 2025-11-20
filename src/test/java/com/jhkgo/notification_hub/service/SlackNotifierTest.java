package com.jhkgo.notification_hub.service;

import com.jhkgo.notification_hub.config.SlackProperties;
import com.jhkgo.notification_hub.domain.entity.Notification;
import com.jhkgo.notification_hub.domain.entity.NotificationDelivery;
import com.jhkgo.notification_hub.domain.enums.DeliveryChannel;
import com.jhkgo.notification_hub.domain.enums.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.ExpectedCount.times;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class SlackNotifierTest {

    private RestTemplate restTemplate;
    private MockRestServiceServer server;
    private SlackNotifier slackNotifier;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        slackNotifier = new SlackNotifier(restTemplate, new SlackProperties("https://hooks.slack.test/webhook"));
    }

    @Test
    void shouldSendSlackMessageWithNotificationDetails() {
        Notification notification = new Notification(
            NotificationType.ORDER_COMPLETED,
            "주문 완료",
            "주문 번호 1234가 완료되었습니다",
            "customer-1",
            null
        );
        NotificationDelivery delivery = new NotificationDelivery(DeliveryChannel.SLACK, "ops-channel");
        notification.addDelivery(delivery);

        server.expect(times(1), requestTo("https://hooks.slack.test/webhook"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json("""
                {"text":"[ORDER_COMPLETED] 주문 완료 - 주문 번호 1234가 완료되었습니다"}
                """, true))
            .andRespond(withSuccess());

        DeliveryExecutionResult result = slackNotifier.send(delivery);

        assertThat(result.succeeded()).isTrue();
        server.verify();
    }

    @Test
    void shouldReturnFailureWhenSlackRespondsWithError() {
        Notification notification = new Notification(
            NotificationType.SYSTEM_ALERT,
            "장애 발생",
            "서비스 다운",
            "operator",
            null
        );
        NotificationDelivery delivery = new NotificationDelivery(DeliveryChannel.SLACK, "ops-channel");
        notification.addDelivery(delivery);

        server.expect(times(1), requestTo("https://hooks.slack.test/webhook"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withServerError());

        DeliveryExecutionResult result = slackNotifier.send(delivery);

        assertThat(result.succeeded()).isFalse();
        assertThat(result.errorMessage()).isNotEmpty();
    }
}
