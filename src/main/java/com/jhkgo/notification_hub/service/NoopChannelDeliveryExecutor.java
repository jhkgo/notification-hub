package com.jhkgo.notification_hub.service;

import com.jhkgo.notification_hub.domain.entity.NotificationDelivery;
import org.springframework.stereotype.Component;

@Component
public class NoopChannelDeliveryExecutor implements ChannelDeliveryExecutor {

    @Override
    public void execute(NotificationDelivery delivery) {
        // 실제 채널 전송 구현은 이후 단계에서 채워진다.
    }
}
