package com.jhkgo.notification_hub.controller;

import com.jhkgo.notification_hub.dto.NotificationRequest;
import com.jhkgo.notification_hub.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
