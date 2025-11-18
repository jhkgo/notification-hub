package com.jhkgo.notification_hub.service;

public record DeliveryExecutionResult(boolean succeeded, String errorMessage) {

    public static DeliveryExecutionResult success() {
        return new DeliveryExecutionResult(true, null);
    }

    public static DeliveryExecutionResult failure(String errorMessage) {
        return new DeliveryExecutionResult(false, errorMessage);
    }
}
