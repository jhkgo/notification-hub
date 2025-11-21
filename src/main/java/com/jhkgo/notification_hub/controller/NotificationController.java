package com.jhkgo.notification_hub.controller;

import com.jhkgo.notification_hub.dto.NotificationRequest;
import com.jhkgo.notification_hub.dto.NotificationPageResponse;
import com.jhkgo.notification_hub.dto.DeliveryResponse;
import com.jhkgo.notification_hub.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody NotificationRequest request) {
        notificationService.create(request);
        return ResponseEntity.accepted().build();
    }

    @GetMapping
    public ResponseEntity<NotificationPageResponse> list(Pageable pageable) {
        return ResponseEntity.ok(notificationService.list(pageable));
    }

    @GetMapping("/{notificationId}/deliveries")
    public ResponseEntity<List<DeliveryResponse>> listDeliveries(@PathVariable Long notificationId) {
        return ResponseEntity.ok(notificationService.listDeliveries(notificationId));
    }
}
