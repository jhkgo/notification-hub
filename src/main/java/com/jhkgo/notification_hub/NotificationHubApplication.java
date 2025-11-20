package com.jhkgo.notification_hub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import com.jhkgo.notification_hub.config.DeliveryWorkerProperties;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(DeliveryWorkerProperties.class)
public class NotificationHubApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotificationHubApplication.class, args);
	}

}
