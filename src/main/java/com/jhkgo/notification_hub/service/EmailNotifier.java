package com.jhkgo.notification_hub.service;

import com.jhkgo.notification_hub.domain.entity.Notification;
import com.jhkgo.notification_hub.domain.entity.NotificationDelivery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EmailNotifier {

    private static final Logger log = LoggerFactory.getLogger(EmailNotifier.class);

    public DeliveryExecutionResult send(NotificationDelivery delivery) {
        Notification notification = delivery.getNotification();
        String title = notification != null ? notification.getTitle() : "Notification";
        String message = notification != null ? notification.getMessage() : "";

        log.info("Mock Email to {} | {} - {}", delivery.getRecipient(), title, message);
        return DeliveryExecutionResult.success();
    }
}
