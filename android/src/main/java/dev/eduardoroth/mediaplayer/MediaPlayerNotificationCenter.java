package dev.eduardoroth.mediaplayer;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.getcapacitor.JSObject;

import java.util.ArrayList;
import java.util.List;


public class MediaPlayerNotificationCenter extends ViewModel {
    public enum NOTIFICATION_TYPE {
        MEDIA_PLAYER_READY {
            @NonNull
            @Override
            public String toString() {
                return "MediaPlayer:Ready";
            }
        }, MEDIA_PLAYER_PLAY {
            @NonNull
            @Override
            public String toString() {
                return "MediaPlayer:Play";
            }
        }, MEDIA_PLAYER_PAUSE {
            @NonNull
            @Override
            public String toString() {
                return "MediaPlayer:Pause";
            }
        }, MEDIA_PLAYER_ENDED {
            @NonNull
            @Override
            public String toString() {
                return "MediaPlayer:Ended";
            }
        }, MEDIA_PLAYER_REMOVED {
            @NonNull
            @Override
            public String toString() {
                return "MediaPlayer:Removed";
            }
        }, MEDIA_PLAYER_SEEK {
            @NonNull
            @Override
            public String toString() {
                return "MediaPlayer:Seek";
            }
        }, MEDIA_PLAYER_TIME_UPDATED {
            @NonNull
            @Override
            public String toString() {
                return "MediaPlayer:TimeUpdated";
            }
        }, MEDIA_PLAYER_FULLSCREEN {
            @NonNull
            @Override
            public String toString() {
                return "MediaPlayer:FullScreen";
            }
        }, MEDIA_PLAYER_PIP {
            @NonNull
            @Override
            public String toString() {
                return "MediaPlayer:PictureInPicture";
            }
        }, MEDIA_PLAYER_BACKGROUND_PLAYING {
            @NonNull
            @Override
            public String toString() {
                return "MediaPlayer:isPlayingInBackground";
            }
        }
    }

    public record CapacitorNotification(String getEventName, JSObject getData) {
    }

    public interface OnNextNotification {
        void send(CapacitorNotification notification);
    }

    private final LifecycleOwner _owner;
    private final List<CapacitorNotification> _notifications = new ArrayList<>();
    private final MutableLiveData<ArrayList<CapacitorNotification>> _pendingNotifications = new MutableLiveData<>(new ArrayList<>());
    private static MediaPlayerNotificationCenter _notificationCenter;

    private MediaPlayerNotificationCenter(LifecycleOwner owner) {
        _owner = owner;
    }

    public static void init(LifecycleOwner owner) {
        _notificationCenter = new MediaPlayerNotificationCenter(owner);
    }

    public static void listenNotifications(OnNextNotification onNextNotification) {
        if (_notificationCenter == null) {
            throw new Error("You need to initialize the Notification Center before using it");
        }
        _notificationCenter._pendingNotifications.observe(_notificationCenter._owner, pendingNotifications -> {
            if (!pendingNotifications.isEmpty()) {
                CapacitorNotification nextNotification = pendingNotifications.get(0);
                if (nextNotification != null) {
                    onNextNotification.send(nextNotification);

                    ArrayList<CapacitorNotification> updatedList = _notificationCenter._pendingNotifications.getValue();
                    assert updatedList != null;
                    updatedList.remove(0);
                    _notificationCenter._pendingNotifications.setValue(updatedList);
                }
            }
        });
    }

    public static void post(CapacitorNotification notification) {
        if (_notificationCenter == null) {
            throw new Error("You need to initialize the Notification Center before using it");
        }
        _notificationCenter._notifications.add(notification);
        ArrayList<CapacitorNotification> updatedList = _notificationCenter._pendingNotifications.getValue();
        assert updatedList != null;
        updatedList.add(notification);
        _notificationCenter._pendingNotifications.setValue(updatedList);
    }

}
