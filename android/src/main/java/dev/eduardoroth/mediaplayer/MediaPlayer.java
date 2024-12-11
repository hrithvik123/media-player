package dev.eduardoroth.mediaplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.media3.common.C;
import androidx.media3.common.util.UnstableApi;

import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;

import java.util.HashMap;

import dev.eduardoroth.mediaplayer.models.AndroidOptions;
import dev.eduardoroth.mediaplayer.models.ExtraOptions;
import dev.eduardoroth.mediaplayer.models.MediaPlayerNotification;
import dev.eduardoroth.mediaplayer.state.MediaPlayerState;
import dev.eduardoroth.mediaplayer.state.MediaPlayerStateProvider;

@UnstableApi
public class MediaPlayer {
    public static long VIDEO_STEP = 10000;
    private final AppCompatActivity _currentActivity;

    MediaPlayer(AppCompatActivity currentActivity) {
        _currentActivity = currentActivity;
    }

    @UnstableApi
    public void create(PluginCall call, String playerId, String url, AndroidOptions android, ExtraOptions extra) {
        MediaPlayerState playerState = MediaPlayerStateProvider.getState(playerId);
        if (playerState != null) {
            JSObject ret = new JSObject();
            ret.put("method", "create");
            ret.put("result", false);
            ret.put("message", "Player with id " + playerId + " is already created");
            call.resolve(ret);
            return;
        }

        _currentActivity.runOnUiThread(() -> {
            MediaPlayerContainer playerContainer = new MediaPlayerContainer(_currentActivity, url, playerId, android, extra);
            _currentActivity.getSupportFragmentManager().beginTransaction().add(R.id.MediaPlayerFragmentContainerView, playerContainer, playerId).commit();
            JSObject ret = new JSObject();
            ret.put("method", "create");
            ret.put("result", true);
            ret.put("value", playerId);
            call.resolve(ret);
        });
    }

    public void play(PluginCall call, String playerId) {
        MediaPlayerState playerState = MediaPlayerStateProvider.getState(playerId);
        if (playerState == null) {
            JSObject ret = new JSObject();
            ret.put("method", "play");
            ret.put("result", false);
            ret.put("message", "Player not found");
            call.resolve(ret);
            return;
        }
        playerState.playerController.get().play();
        JSObject ret = new JSObject();
        ret.put("method", "play");
        ret.put("result", true);
        ret.put("value", true);
        call.resolve(ret);
    }

    public void pause(PluginCall call, String playerId) {
        MediaPlayerState playerState = MediaPlayerStateProvider.getState(playerId);
        if (playerState == null) {
            JSObject ret = new JSObject();
            ret.put("method", "pause");
            ret.put("result", false);
            ret.put("message", "Player not found");
            call.resolve(ret);
            return;
        }
        playerState.playerController.get().pause();
        JSObject ret = new JSObject();
        ret.put("method", "pause");
        ret.put("result", true);
        ret.put("value", true);
        call.resolve(ret);
    }

    public void getDuration(PluginCall call, String playerId) {
        MediaPlayerState playerState = MediaPlayerStateProvider.getState(playerId);
        if (playerState == null) {
            JSObject ret = new JSObject();
            ret.put("method", "getDuration");
            ret.put("result", false);
            ret.put("message", "Player not found");
            call.resolve(ret);
            return;
        }
        long duration = playerState.playerController.get().getDuration();
        JSObject ret = new JSObject();
        ret.put("method", "getDuration");
        ret.put("result", true);
        ret.put("value", duration == C.TIME_UNSET ? 0 : (duration / 1000));
        call.resolve(ret);
    }

    public void getCurrentTime(PluginCall call, String playerId) {
        MediaPlayerState playerState = MediaPlayerStateProvider.getState(playerId);
        if (playerState == null) {
            JSObject ret = new JSObject();
            ret.put("method", "getCurrentTime");
            ret.put("result", false);
            ret.put("message", "Player not found");
            call.resolve(ret);
            return;
        }
        long currentTime = playerState.playerController.get().getCurrentTime();
        JSObject ret = new JSObject();
        ret.put("method", "getCurrentTime");
        ret.put("result", true);
        ret.put("value", currentTime == C.TIME_UNSET ? 0 : (currentTime / 1000));
        call.resolve(ret);
    }

    public void setCurrentTime(PluginCall call, String playerId, long time) {
        MediaPlayerState playerState = MediaPlayerStateProvider.getState(playerId);
        if (playerState == null) {
            JSObject ret = new JSObject();
            ret.put("method", "setCurrentTime");
            ret.put("result", false);
            ret.put("message", "Player not found");
            call.resolve(ret);
            return;
        }
        long updatedTime = playerState.playerController.get().setCurrentTime(time);
        JSObject ret = new JSObject();
        ret.put("method", "setCurrentTime");
        ret.put("result", true);
        ret.put("value", updatedTime);
        call.resolve(ret);
    }

    public void isPlaying(PluginCall call, String playerId) {
        MediaPlayerState playerState = MediaPlayerStateProvider.getState(playerId);
        if (playerState == null) {
            JSObject ret = new JSObject();
            ret.put("method", "isPlaying");
            ret.put("result", false);
            ret.put("message", "Player not found");
            call.resolve(ret);
            return;
        }

        JSObject ret = new JSObject();
        ret.put("method", "isPlaying");
        ret.put("result", true);
        ret.put("value", playerState.playerController.get().isPlaying());
        call.resolve(ret);
    }

    public void isMuted(PluginCall call, String playerId) {
        MediaPlayerState playerState = MediaPlayerStateProvider.getState(playerId);
        if (playerState == null) {
            JSObject ret = new JSObject();
            ret.put("method", "isMuted");
            ret.put("result", false);
            ret.put("message", "Player not found");
            call.resolve(ret);
            return;
        }
        JSObject ret = new JSObject();
        ret.put("method", "isMuted");
        ret.put("result", true);
        ret.put("value", playerState.playerController.get().isMuted());
        call.resolve(ret);
    }

    public void mute(PluginCall call, String playerId) {
        MediaPlayerState playerState = MediaPlayerStateProvider.getState(playerId);
        if (playerState == null) {
            JSObject ret = new JSObject();
            ret.put("method", "mute");
            ret.put("result", false);
            ret.put("message", "Player not found");
            call.resolve(ret);
            return;
        }
        playerState.playerController.get().mute();
        JSObject ret = new JSObject();
        ret.put("method", "mute");
        ret.put("result", true);
        ret.put("value", true);
        call.resolve(ret);
    }

    public void getVolume(PluginCall call, String playerId) {
        MediaPlayerState playerState = MediaPlayerStateProvider.getState(playerId);
        if (playerState == null) {
            JSObject ret = new JSObject();
            ret.put("method", "getVolume");
            ret.put("result", false);
            ret.put("message", "Player not found");
            call.resolve(ret);
            return;
        }
        JSObject ret = new JSObject();
        ret.put("method", "getVolume");
        ret.put("result", true);
        ret.put("value", playerState.playerController.get().getVolume());
        call.resolve(ret);
    }

    public void setVolume(PluginCall call, String playerId, Double volume) {
        MediaPlayerState playerState = MediaPlayerStateProvider.getState(playerId);
        if (playerState == null) {
            JSObject ret = new JSObject();
            ret.put("method", "setVolume");
            ret.put("result", false);
            ret.put("message", "Player not found");
            call.resolve(ret);
            return;
        }
        playerState.playerController.get().setVolume(volume.floatValue());
        JSObject ret = new JSObject();
        ret.put("method", "setVolume");
        ret.put("result", true);
        ret.put("value", volume);
        call.resolve(ret);
    }

    public void getRate(PluginCall call, String playerId) {
        MediaPlayerState playerState = MediaPlayerStateProvider.getState(playerId);
        if (playerState == null) {
            JSObject ret = new JSObject();
            ret.put("method", "getRate");
            ret.put("result", false);
            ret.put("message", "Player not found");
            call.resolve(ret);
            return;
        }
        JSObject ret = new JSObject();
        ret.put("method", "getRate");
        ret.put("result", true);
        ret.put("value", playerState.playerController.get().getRate());
        call.resolve(ret);
    }

    public void setRate(PluginCall call, String playerId, Double rate) {
        MediaPlayerState playerState = MediaPlayerStateProvider.getState(playerId);
        if (playerState == null) {
            JSObject ret = new JSObject();
            ret.put("method", "setRate");
            ret.put("result", false);
            ret.put("message", "Player not found");
            call.resolve(ret);
            return;
        }
        playerState.playerController.get().setRate(rate.floatValue());
        JSObject ret = new JSObject();
        ret.put("method", "setRate");
        ret.put("result", true);
        ret.put("value", rate);
        call.resolve(ret);
    }

    public void remove(PluginCall call, String playerId) {
        MediaPlayerState playerState = MediaPlayerStateProvider.getState(playerId);
        if (playerState == null) {
            JSObject ret = new JSObject();
            ret.put("method", "remove");
            ret.put("result", false);
            ret.put("message", "Player not found");
            call.resolve(ret);
            return;
        }

        _currentActivity.runOnUiThread(() -> {
            Fragment playerFragment = _currentActivity.getSupportFragmentManager().findFragmentByTag(playerId);
            if (playerFragment != null) {
                _currentActivity.getSupportFragmentManager().beginTransaction().remove(playerFragment).commit();
                MediaPlayerStateProvider.clearState(playerId);
            }
            MediaPlayerNotificationCenter.post(
                    MediaPlayerNotification.create(playerId, MediaPlayerNotificationCenter.NOTIFICATION_TYPE.MEDIA_PLAYER_REMOVED)
                            .build()
            );
            JSObject ret = new JSObject();
            ret.put("method", "remove");
            ret.put("result", true);
            ret.put("value", playerId);
            call.resolve(ret);
        });
    }

    public void removeAll(PluginCall call) {
        _currentActivity.runOnUiThread(() -> {
            _currentActivity.getSupportFragmentManager().getFragments().forEach(fragment -> {
                String playerId = fragment.getTag();
                _currentActivity.getSupportFragmentManager().beginTransaction().remove(fragment).commit();
                MediaPlayerStateProvider.clearState(playerId);
                MediaPlayerNotificationCenter.post(
                        MediaPlayerNotification.create(playerId, MediaPlayerNotificationCenter.NOTIFICATION_TYPE.MEDIA_PLAYER_REMOVED)
                                .build()
                );
            });
            JSObject ret = new JSObject();
            ret.put("method", "removeAll");
            ret.put("result", true);
            ret.put("value", "[]");
            call.resolve(ret);
        });
    }
}
