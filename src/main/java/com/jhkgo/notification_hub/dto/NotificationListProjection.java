package com.jhkgo.notification_hub.dto;

import com.jhkgo.notification_hub.domain.enums.NotificationType;

public interface NotificationListProjection {
    Long getId();
    NotificationType getType();
    String getTitle();
    String getMessage();
    String getRecipientId();
    long getTotalCount();
    long getSucceedCount();
    long getFailedCount();
}
