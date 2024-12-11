package dev.eduardoroth.mediaplayer.models;

import com.getcapacitor.JSObject;

import dev.eduardoroth.mediaplayer.MediaPlayerNotificationCenter.CapacitorNotification;
import dev.eduardoroth.mediaplayer.MediaPlayerNotificationCenter.NOTIFICATION_TYPE;

public class MediaPlayerNotification {

    public static MediaPlayerNotification create(NOTIFICATION_TYPE notificationType) {
        return create(null, notificationType);
    }

    public static MediaPlayerNotification create(String playerId, NOTIFICATION_TYPE notificationType) {
        MediaPlayerNotification newMediaPlayerNotification = new MediaPlayerNotification(notificationType);
        if (playerId != null) {
            newMediaPlayerNotification.addData("playerId", playerId);
        }
        return newMediaPlayerNotification;
    }

    private final NOTIFICATION_TYPE _notificationType;
    private final JSObject _notificationData;

    private MediaPlayerNotification(NOTIFICATION_TYPE notificationType) {
        _notificationType = notificationType;
        _notificationData = new JSObject();
    }

    public <T> MediaPlayerNotification addData(String key, T value) {
        _notificationData.put(key, value);
        return this;
    }

    public CapacitorNotification build() {
        return new CapacitorNotification(_notificationType.toString(), _notificationData);
    }

}
