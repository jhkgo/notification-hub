package com.jhkgo.notification_hub.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jhkgo.notification_hub.domain.entity.Notification;
import com.jhkgo.notification_hub.domain.entity.NotificationDelivery;
import com.jhkgo.notification_hub.dto.NotificationRequest;
import com.jhkgo.notification_hub.dto.NotificationPageResponse;
import com.jhkgo.notification_hub.dto.NotificationResponse;
import com.jhkgo.notification_hub.dto.NotificationListProjection;
import com.jhkgo.notification_hub.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.List;

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

    @Transactional(readOnly = true)
    public NotificationPageResponse list(Pageable pageable) {
        Page<NotificationListProjection> pageResult = notificationRepository.findAllWithSummary(pageable);
        List<NotificationResponse> content = pageResult.stream()
            .map(this::toResponse)
            .toList();

        return new NotificationPageResponse(
            content,
            pageResult.getNumber(),
            pageResult.getSize(),
            pageResult.getTotalElements(),
            pageResult.getTotalPages()
        );
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

    private NotificationResponse toResponse(NotificationListProjection projection) {
        return new NotificationResponse(
            projection.getId(),
            projection.getType(),
            projection.getTitle(),
            projection.getMessage(),
            projection.getRecipientId(),
            new NotificationResponse.DeliverySummary(
                (int) projection.getTotalCount(),
                (int) projection.getSucceedCount(),
                (int) projection.getFailedCount()
            )
        );
    }
}
