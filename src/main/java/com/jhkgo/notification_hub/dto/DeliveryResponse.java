package com.jhkgo.notification_hub.dto;

import com.jhkgo.notification_hub.domain.enums.DeliveryChannel;
import com.jhkgo.notification_hub.domain.enums.DeliveryStatus;

import java.time.LocalDateTime;

public record DeliveryResponse(
    Long id,
    DeliveryChannel channel,
    DeliveryStatus status,
    String recipient,
    LocalDateTime sentAt,
    String errorMessage
) {}
