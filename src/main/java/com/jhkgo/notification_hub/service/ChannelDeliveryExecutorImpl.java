package com.jhkgo.notification_hub.service;

import com.jhkgo.notification_hub.domain.entity.NotificationDelivery;
import com.jhkgo.notification_hub.domain.enums.DeliveryChannel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChannelDeliveryExecutorImpl implements ChannelDeliveryExecutor {

    private final SlackNotifier slackNotifier;
    private final EmailNotifier emailNotifier;

    @Override
    public DeliveryExecutionResult execute(NotificationDelivery delivery) {
        if (delivery.getChannel() == DeliveryChannel.SLACK) {
            return slackNotifier.send(delivery);
        }
        if (delivery.getChannel() == DeliveryChannel.EMAIL) {
            return emailNotifier.send(delivery);
        }
        return DeliveryExecutionResult.success();
    }
}
