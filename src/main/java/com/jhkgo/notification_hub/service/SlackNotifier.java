package com.jhkgo.notification_hub.service;

import com.jhkgo.notification_hub.config.SlackProperties;
import com.jhkgo.notification_hub.domain.entity.Notification;
import com.jhkgo.notification_hub.domain.entity.NotificationDelivery;
import com.jhkgo.notification_hub.domain.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientResponseException;

@Component
@RequiredArgsConstructor
public class SlackNotifier {

    private final RestTemplate restTemplate;
    private final SlackProperties slackProperties;

    public DeliveryExecutionResult send(NotificationDelivery delivery) {
        String webhookUrl = slackProperties.webhookUrl();
        if (!StringUtils.hasText(webhookUrl)) {
            return DeliveryExecutionResult.failure("Slack webhook URL is not configured");
        }

        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(
                webhookUrl,
                new SlackPayload(buildMessageText(delivery)),
                Void.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                return DeliveryExecutionResult.success();
            }
            return DeliveryExecutionResult.failure(response.getStatusCode().toString());
        } catch (RestClientResponseException ex) {
            String message = StringUtils.hasText(ex.getResponseBodyAsString())
                ? ex.getResponseBodyAsString()
                : ex.getStatusText();
            return DeliveryExecutionResult.failure(message);
        } catch (RestClientException ex) {
            return DeliveryExecutionResult.failure(ex.getMessage());
        }
    }

    private String buildMessageText(NotificationDelivery delivery) {
        Notification notification = delivery.getNotification();
        NotificationType type = notification != null ? notification.getType() : null;
        String title = notification != null ? notification.getTitle() : "Notification";
        String message = notification != null ? notification.getMessage() : "";

        String prefix = type != null ? type.name() : "NOTIFICATION";
        return "[" + prefix + "] " + title + (message.isBlank() ? "" : " - " + message);
    }

    private record SlackPayload(String text) {}
}
