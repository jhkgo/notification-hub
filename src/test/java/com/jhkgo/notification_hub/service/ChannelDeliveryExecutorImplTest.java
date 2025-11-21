package com.jhkgo.notification_hub.service;

import com.jhkgo.notification_hub.domain.entity.NotificationDelivery;
import com.jhkgo.notification_hub.domain.enums.DeliveryChannel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChannelDeliveryExecutorImplTest {

    @Mock
    private SlackNotifier slackNotifier;

    @Mock
    private EmailNotifier emailNotifier;

    @InjectMocks
    private ChannelDeliveryExecutorImpl executor;

    @DisplayName("EMAIL 채널이면 EmailNotifier로 위임하고 성공 결과를 반환한다")
    @Test
    void shouldDelegateEmailDeliveryToEmailNotifier() {
        NotificationDelivery delivery = new NotificationDelivery(DeliveryChannel.EMAIL, "user@example.com");
        when(emailNotifier.send(any(NotificationDelivery.class))).thenReturn(DeliveryExecutionResult.success());

        DeliveryExecutionResult result = executor.execute(delivery);

        verify(emailNotifier).send(delivery);
        assertThat(result.succeeded()).isTrue();
    }

    @DisplayName("EmailNotifier가 실패를 반환하면 실패 결과를 그대로 전달한다")
    @Test
    void shouldReturnFailureWhenEmailNotifierFails() {
        NotificationDelivery delivery = new NotificationDelivery(DeliveryChannel.EMAIL, "user@example.com");
        when(emailNotifier.send(any(NotificationDelivery.class))).thenReturn(DeliveryExecutionResult.failure("email error"));

        DeliveryExecutionResult result = executor.execute(delivery);

        assertThat(result.succeeded()).isFalse();
        assertThat(result.errorMessage()).isEqualTo("email error");
    }

    @DisplayName("SLACK 채널이면 SlackNotifier로 위임하고 성공 결과를 반환한다")
    @Test
    void shouldDelegateSlackDeliveryToSlackNotifier() {
        NotificationDelivery delivery = new NotificationDelivery(DeliveryChannel.SLACK, "https://webhook");
        when(slackNotifier.send(any(NotificationDelivery.class))).thenReturn(DeliveryExecutionResult.success());

        DeliveryExecutionResult result = executor.execute(delivery);

        verify(slackNotifier).send(delivery);
        assertThat(result.succeeded()).isTrue();
    }

    @DisplayName("지원하지 않는 채널이면 실패 결과를 반환한다")
    @Test
    void shouldReturnFailureForUnsupportedChannel() {
        NotificationDelivery delivery = new NotificationDelivery(null, "target");

        DeliveryExecutionResult result = executor.execute(delivery);

        assertThat(result.succeeded()).isFalse();
        assertThat(result.errorMessage()).contains("Unsupported channel");
    }

    @DisplayName("SlackNotifier가 실패를 반환하면 실패 결과를 그대로 전달한다")
    @Test
    void shouldReturnFailureWhenSlackNotifierFails() {
        NotificationDelivery delivery = new NotificationDelivery(DeliveryChannel.SLACK, "https://webhook");
        when(slackNotifier.send(any(NotificationDelivery.class))).thenReturn(DeliveryExecutionResult.failure("slack error"));

        DeliveryExecutionResult result = executor.execute(delivery);

        assertThat(result.succeeded()).isFalse();
        assertThat(result.errorMessage()).isEqualTo("slack error");
    }
}
