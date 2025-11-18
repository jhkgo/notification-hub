package com.jhkgo.notification_hub.service;

import com.jhkgo.notification_hub.domain.entity.NotificationDelivery;

public interface ChannelDeliveryExecutor {

    void execute(NotificationDelivery delivery);
}
