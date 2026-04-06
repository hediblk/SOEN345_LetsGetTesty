package com.letsgettesty.backend.notification;

import com.letsgettesty.backend.model.Notification;

public interface NotificationRepository {

    Notification insert(Notification notification);
}
