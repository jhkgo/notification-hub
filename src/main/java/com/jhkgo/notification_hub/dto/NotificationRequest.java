package com.jhkgo.notification_hub.dto;

import com.jhkgo.notification_hub.domain.enums.DeliveryChannel;
import com.jhkgo.notification_hub.domain.enums.NotificationType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

public record NotificationRequest(
    @NotNull(message = "알림 타입은 필수입니다")
    NotificationType type,

    @NotBlank(message = "제목은 필수입니다")
    String title,

    @NotBlank(message = "메시지는 필수입니다")
    String message,

    @NotBlank(message = "수신자 ID는 필수입니다")
    String recipientId,

    Map<String, Object> metadata,

    @NotEmpty(message = "전송 대상은 최소 1개 이상이어야 합니다")
    List<@Valid DeliveryRequest> deliveries
) {

    private record DeliveryRequest(
        @NotNull(message = "전송 채널은 필수입니다")
        DeliveryChannel channel,

        @NotBlank(message = "수신처는 필수입니다")
        String recipient
    ) {}
}
