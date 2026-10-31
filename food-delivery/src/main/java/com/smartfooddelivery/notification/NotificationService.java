package com.smartfooddelivery.notification;

import com.smartfooddelivery.model.User;

public interface NotificationService {

    void sendNotification(User customer, String message);
}