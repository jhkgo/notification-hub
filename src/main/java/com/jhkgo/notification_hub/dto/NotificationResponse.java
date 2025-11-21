package com.jhkgo.notification_hub.dto;

import com.jhkgo.notification_hub.domain.enums.NotificationType;
import com.jhkgo.notification_hub.domain.enums.NotificationProgressStatus;

public record NotificationResponse(
    Long id,
    NotificationType type,
    NotificationProgressStatus progressStatus,
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
