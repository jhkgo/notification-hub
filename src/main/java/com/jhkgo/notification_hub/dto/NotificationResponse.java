package com.jhkgo.notification_hub.dto;

import com.jhkgo.notification_hub.domain.enums.NotificationType;

public record NotificationResponse(
    Long id,
    NotificationType type,
    String title,
    String message,
    String recipientId,
    DeliverySummary deliverySummary
) {

    public record DeliverySummary(
        int totalCount,
        int succeedCount,
        int failedCount
    ) {}
}
