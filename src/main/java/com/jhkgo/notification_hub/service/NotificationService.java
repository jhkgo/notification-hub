package com.jhkgo.notification_hub.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jhkgo.notification_hub.domain.entity.Notification;
import com.jhkgo.notification_hub.domain.entity.NotificationDelivery;
import com.jhkgo.notification_hub.dto.NotificationRequest;
import com.jhkgo.notification_hub.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void create(NotificationRequest request) {
        Notification notification = new Notification(
            request.type(),
            request.title(),
            request.message(),
            request.recipientId(),
            toMetadataString(request.metadata())
        );

        for (NotificationRequest.DeliveryRequest deliveryRequest : request.deliveries()) {
            NotificationDelivery delivery = new NotificationDelivery(
                deliveryRequest.channel(),
                deliveryRequest.recipient()
            );
            notification.addDelivery(delivery);
        }

        notificationRepository.save(notification);
    }

    private String toMetadataString(Map<String, Object> metadata) {
        if (metadata == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("메타데이터를 문자열로 변환할 수 없습니다", e);
        }
    }
}
