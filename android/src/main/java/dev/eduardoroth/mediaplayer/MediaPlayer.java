package dev.eduardoroth.mediaplayer;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.media3.common.C;
import androidx.media3.common.util.UnstableApi;

import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;

import java.io.File;

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
        JSObject ret = new JSObject();
        ret.put("method", "create");
        try {
            MediaPlayerStateProvider.getState(playerId);
            ret.put("result", false);
            ret.put("message", "Player with id " + playerId + " is already created");
        } catch (Error err) {
            extra.poster = extra.poster != null ? getFinalPath(extra.poster) : null;
            MediaPlayerContainer playerContainer = new MediaPlayerContainer(_currentActivity, getFinalPath(url), playerId, android, extra);
            _currentActivity.getSupportFragmentManager().beginTransaction().add(R.id.MediaPlayerFragmentContainerView, playerContainer, playerId).commit();
            ret.put("result", true);
            ret.put("value", playerId);
        }
        call.resolve(ret);
    }

    public void play(PluginCall call, String playerId) {
        JSObject ret = new JSObject();
        ret.put("method", "play");
        try {
            MediaPlayerState playerState = MediaPlayerStateProvider.getState(playerId);
            playerState.playerController.get().play();
            ret.put("result", true);
            ret.put("value", true);
        } catch (Error err) {
            ret.put("result", false);
            ret.put("message", "Player not found");
        }
        call.resolve(ret);
    }

    public void pause(PluginCall call, String playerId) {
        JSObject ret = new JSObject();
        ret.put("method", "pause");
        try {
            MediaPlayerState playerState = MediaPlayerStateProvider.getState(playerId);
            playerState.playerController.get().pause();
            ret.put("result", true);
            ret.put("value", true);
        } catch (Error err) {
            ret.put("result", false);
            ret.put("message", "Player not found");
        }
        call.resolve(ret);
    }

    public void getDuration(PluginCall call, String playerId) {
        JSObject ret = new JSObject();
        ret.put("method", "getDuration");
        try {
            MediaPlayerState playerState = MediaPlayerStateProvider.getState(playerId);
            long duration = playerState.playerController.get().getDuration();
            ret.put("result", true);
            ret.put("value", duration == C.TIME_UNSET ? 0 : (duration / 1000));
        } catch (Error err) {
            ret.put("result", false);
            ret.put("message", "Player not found");
        }
        call.resolve(ret);
    }

    public void getCurrentTime(PluginCall call, String playerId) {
        JSObject ret = new JSObject();
        ret.put("method", "getCurrentTime");
        try {
            MediaPlayerState playerState = MediaPlayerStateProvider.getState(playerId);
            long currentTime = playerState.playerController.get().getCurrentTime();
            ret.put("result", true);
            ret.put("value", currentTime == C.TIME_UNSET ? 0 : (currentTime / 1000));
        } catch (Error err) {
            ret.put("result", false);
            ret.put("message", "Player not found");
        }
        call.resolve(ret);
    }

    public void setCurrentTime(PluginCall call, String playerId, long time) {
        JSObject ret = new JSObject();
        ret.put("method", "setCurrentTime");
        try {
            MediaPlayerState playerState = MediaPlayerStateProvider.getState(playerId);
            long updatedTime = playerState.playerController.get().setCurrentTime(time);
            ret.put("result", true);
            ret.put("value", updatedTime);
        } catch (Error err) {
            ret.put("result", false);
            ret.put("message", "Player not found");
        }
        call.resolve(ret);
    }

    public void isPlaying(PluginCall call, String playerId) {
        JSObject ret = new JSObject();
        ret.put("method", "isPlaying");
        try {
            MediaPlayerState playerState = MediaPlayerStateProvider.getState(playerId);
            ret.put("result", true);
            ret.put("value", playerState.playerController.get().isPlaying());
        } catch (Error err) {
            ret.put("result", false);
            ret.put("message", "Player not found");
        }
        call.resolve(ret);
    }

    public void isMuted(PluginCall call, String playerId) {
        JSObject ret = new JSObject();
        ret.put("method", "isMuted");
        try {
            MediaPlayerState playerState = MediaPlayerStateProvider.getState(playerId);
            ret.put("result", true);
            ret.put("value", playerState.playerController.get().isMuted());
        } catch (Error err) {
            ret.put("result", false);
            ret.put("message", "Player not found");
        }
        call.resolve(ret);
    }

    public void mute(PluginCall call, String playerId) {
        JSObject ret = new JSObject();
        ret.put("method", "mute");
        try {
            MediaPlayerState playerState = MediaPlayerStateProvider.getState(playerId);
            playerState.playerController.get().mute();
            ret.put("result", true);
            ret.put("value", true);
        } catch (Error err) {
            ret.put("result", false);
            ret.put("message", "Player not found");
        }
        call.resolve(ret);
    }

    public void getVolume(PluginCall call, String playerId) {
        JSObject ret = new JSObject();
        ret.put("method", "getVolume");
        try {
            MediaPlayerState playerState = MediaPlayerStateProvider.getState(playerId);
            ret.put("result", true);
            ret.put("value", playerState.playerController.get().getVolume());
        } catch (Error err) {
            ret.put("result", false);
            ret.put("message", "Player not found");
        }
        call.resolve(ret);
    }

    public void setVolume(PluginCall call, String playerId, Double volume) {
        JSObject ret = new JSObject();
        ret.put("method", "setVolume");
        try {
            MediaPlayerState playerState = MediaPlayerStateProvider.getState(playerId);
            playerState.playerController.get().setVolume(volume.floatValue());
            ret.put("result", true);
            ret.put("value", volume);
        } catch (Error err) {
            ret.put("result", false);
            ret.put("message", "Player not found");
        }
        call.resolve(ret);
    }

    public void getRate(PluginCall call, String playerId) {
        JSObject ret = new JSObject();
        ret.put("method", "getRate");
        try {
            MediaPlayerState playerState = MediaPlayerStateProvider.getState(playerId);
            ret.put("result", true);
            ret.put("value", playerState.playerController.get().getRate());
        } catch (Error err) {
            ret.put("result", false);
            ret.put("message", "Player not found");
        }
        call.resolve(ret);
    }

    public void setRate(PluginCall call, String playerId, Double rate) {
        JSObject ret = new JSObject();
        ret.put("method", "setRate");
        try {
            MediaPlayerState playerState = MediaPlayerStateProvider.getState(playerId);
            playerState.playerController.get().setRate(rate.floatValue());
            ret.put("result", true);
            ret.put("value", rate);
        } catch (Error err) {
            ret.put("result", false);
            ret.put("message", "Player not found");
        }
        call.resolve(ret);
    }

    public void remove(PluginCall call, String playerId) {
        JSObject ret = new JSObject();
        ret.put("method", "remove");
        try {
            MediaPlayerState playerState = MediaPlayerStateProvider.getState(playerId);
            Fragment playerFragment = _currentActivity.getSupportFragmentManager().findFragmentByTag(playerId);
            playerState.playerController.get().destroy();
            MediaPlayerStateProvider.clearState(playerId);
            if (playerFragment != null) {
                _currentActivity.getSupportFragmentManager().beginTransaction().remove(playerFragment).commit();
            }
            MediaPlayerNotificationCenter.post(MediaPlayerNotification.create(playerId, MediaPlayerNotificationCenter.NOTIFICATION_TYPE.MEDIA_PLAYER_REMOVED).build());
            ret.put("result", true);
            ret.put("value", playerId);
        } catch (Error err) {
            ret.put("result", false);
            ret.put("message", "Player not found");
        }
        call.resolve(ret);
    }

    public void removeAll(PluginCall call) {
        _currentActivity.getSupportFragmentManager().getFragments().forEach(fragment -> {
            String playerId = fragment.getTag();
            _currentActivity.getSupportFragmentManager().beginTransaction().remove(fragment).commit();
            try {
                MediaPlayerState playerState = MediaPlayerStateProvider.getState(playerId);
                playerState.playerController.get().destroy();
            } catch (Error ignored) {
            }
            MediaPlayerStateProvider.clearState(playerId);
            MediaPlayerNotificationCenter.post(MediaPlayerNotification.create(playerId, MediaPlayerNotificationCenter.NOTIFICATION_TYPE.MEDIA_PLAYER_REMOVED).build());
        });
        JSObject ret = new JSObject();
        ret.put("method", "removeAll");
        ret.put("result", true);
        ret.put("value", "[]");
        call.resolve(ret);
    }

    private String getFinalPath(String url) {
        if (url == null) {
            return null;
        }
        String path = null;
        if (url.startsWith("file:///")) {
            return url;
        } else if (url.startsWith("application") || url.contains("_capacitor_file_")) {
            String filesDir = _currentActivity.getFilesDir() + "/";
            path = filesDir + url.substring(url.lastIndexOf("files/") + 6);
            File file = new File(path);
            if (!file.exists()) {
                Log.e("Media Player", "File not found");
                path = null;
            }
        } else if (url.contains("assets")) {
            path = "file:///android_asset/" + url;
        } else if (url.startsWith("http")) {
            path = url;
        }
        return path;
    }
}
