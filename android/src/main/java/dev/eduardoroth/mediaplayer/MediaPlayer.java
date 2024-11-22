package dev.eduardoroth.mediaplayer;

import android.content.Context;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.media3.common.C;
import androidx.media3.common.PlaybackParameters;

import com.getcapacitor.Bridge;
import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;
import com.google.android.gms.cast.framework.CastContext;

import java.util.HashMap;
import java.util.Map;

import dev.eduardoroth.mediaplayer.models.AndroidOptions;
import dev.eduardoroth.mediaplayer.models.ExtraOptions;
import dev.eduardoroth.mediaplayer.utilities.FragmentHelpers;
import dev.eduardoroth.mediaplayer.utilities.NotificationHelpers;

public class MediaPlayer {

    private final Context context;
    private final CastContext castContext;
    private final Bridge bridge;

    private final Map<String, MediaPlayerFragment> players = new HashMap<String, MediaPlayerFragment>();

    MediaPlayer(Context context, CastContext castContext, Bridge bridge) {
        this.context = context;
        this.castContext = castContext;
        this.bridge = bridge;

        bridge.getActivity().runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        FrameLayout videoLayout = new FrameLayout(context);
                        videoLayout.setTag("VIDEO_ONLY");
                        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
                        params.gravity = Gravity.FILL;
                        videoLayout.setLayoutParams(params);
                        videoLayout.setBackgroundColor(context.getColor(R.color.black));
                        videoLayout.setVisibility(View.GONE);
                        ((ViewGroup) bridge.getWebView().getParent()).addView(videoLayout);
                    }
                }
        );
    }

    public void create(PluginCall call, String playerId, Uri url, AndroidOptions android, ExtraOptions extra) {
        bridge.getActivity()
                .runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                FragmentHelpers fragmentHelpers = new FragmentHelpers(bridge);
                                int layoutId = fragmentHelpers.getIdFromPlayerId(playerId);

                                MediaPlayerFragment player = new MediaPlayerFragment(fragmentHelpers, context, castContext, layoutId, playerId, url, android, extra, bridge.getWebView());
                                ((ViewGroup) bridge.getWebView().getParent()).addView(player.layout);

                                fragmentHelpers.loadFragment(player, player.layoutId);

                                players.put(playerId, player);

                                JSObject ret = new JSObject();
                                ret.put("method", "create");
                                ret.put("result", true);
                                ret.put("value", playerId);
                                call.resolve(ret);
                            }
                        });
    }

    public void play(PluginCall call, String playerId) {
        MediaPlayerFragment player = players.get(playerId);
        if (player == null) {
            JSObject ret = new JSObject();
            ret.put("method", "play");
            ret.put("result", false);
            ret.put("message", "Player not found");
            call.resolve(ret);
            return;
        }
        player.player.play();
        JSObject ret = new JSObject();
        ret.put("method", "play");
        ret.put("result", true);
        ret.put("value", true);
        call.resolve(ret);
    }

    public void pause(PluginCall call, String playerId) {
        MediaPlayerFragment player = players.get(playerId);
        if (player == null) {
            JSObject ret = new JSObject();
            ret.put("method", "pause");
            ret.put("result", false);
            ret.put("message", "Player not found");
            call.resolve(ret);
            return;
        }
        player.player.pause();
        JSObject ret = new JSObject();
        ret.put("method", "pause");
        ret.put("result", true);
        ret.put("value", true);
        call.resolve(ret);
    }

    public void getDuration(PluginCall call, String playerId) {
        MediaPlayerFragment player = players.get(playerId);
        if (player == null) {
            JSObject ret = new JSObject();
            ret.put("method", "getDuration");
            ret.put("result", false);
            ret.put("message", "Player not found");
            call.resolve(ret);
            return;
        }
        JSObject ret = new JSObject();
        ret.put("method", "getDuration");
        ret.put("result", true);
        ret.put("value", player.player.getDuration() == C.TIME_UNSET ? 0 : (player.player.getDuration() / 1000));
        call.resolve(ret);
    }

    public void getCurrentTime(PluginCall call, String playerId) {
        MediaPlayerFragment player = players.get(playerId);
        if (player == null) {
            JSObject ret = new JSObject();
            ret.put("method", "getCurrentTime");
            ret.put("result", false);
            ret.put("message", "Player not found");
            call.resolve(ret);
            return;
        }
        JSObject ret = new JSObject();
        ret.put("method", "getCurrentTime");
        ret.put("result", true);
        ret.put("value", player.player.getCurrentPosition() == C.TIME_UNSET ? 0 : (player.player.getCurrentPosition() / 1000));
        call.resolve(ret);
    }

    public void setCurrentTime(PluginCall call, String playerId, Double time) {
        MediaPlayerFragment player = players.get(playerId);
        if (player == null) {
            JSObject ret = new JSObject();
            ret.put("method", "setCurrentTime");
            ret.put("result", false);
            ret.put("message", "Player not found");
            call.resolve(ret);
            return;
        }
        Double seekPosition = player.player.getCurrentPosition() == C.TIME_UNSET
                ? 0
                : Math.min(Math.max(0, time * 1000), player.player.getDuration() == C.TIME_UNSET ? 0 : player.player.getDuration());
        player.player.seekTo(seekPosition.longValue());
        JSObject ret = new JSObject();
        ret.put("method", "setCurrentTime");
        ret.put("result", true);
        ret.put("value", seekPosition);
        call.resolve(ret);
    }

    public void isPlaying(PluginCall call, String playerId) {
        MediaPlayerFragment player = players.get(playerId);
        if (player == null) {
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
        ret.put("value", player.player.isPlaying());
        call.resolve(ret);
    }

    public void isMuted(PluginCall call, String playerId) {
        MediaPlayerFragment player = players.get(playerId);
        if (player == null) {
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
        ret.put("value", player.player.getVolume() == 0);
        call.resolve(ret);
    }

    public void mute(PluginCall call, String playerId) {
        MediaPlayerFragment player = players.get(playerId);
        if (player == null) {
            JSObject ret = new JSObject();
            ret.put("method", "mute");
            ret.put("result", false);
            ret.put("message", "Player not found");
            call.resolve(ret);
            return;
        }
        player.player.setVolume(0);
        JSObject ret = new JSObject();
        ret.put("method", "mute");
        ret.put("result", true);
        ret.put("value", true);
        call.resolve(ret);
    }

    public void getVolume(PluginCall call, String playerId) {
        MediaPlayerFragment player = players.get(playerId);
        if (player == null) {
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
        ret.put("value", player.player.getVolume());
        call.resolve(ret);
    }

    public void setVolume(PluginCall call, String playerId, Double volume) {
        MediaPlayerFragment player = players.get(playerId);
        if (player == null) {
            JSObject ret = new JSObject();
            ret.put("method", "setVolume");
            ret.put("result", false);
            ret.put("message", "Player not found");
            call.resolve(ret);
            return;
        }
        player.player.setVolume(volume.floatValue());
        JSObject ret = new JSObject();
        ret.put("method", "setVolume");
        ret.put("result", true);
        ret.put("value", volume);
        call.resolve(ret);
    }

    public void getRate(PluginCall call, String playerId) {
        MediaPlayerFragment player = players.get(playerId);
        if (player == null) {
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
        ret.put("value", player.player.getPlaybackParameters().speed);
        call.resolve(ret);
    }

    public void setRate(PluginCall call, String playerId, Double rate) {
        MediaPlayerFragment player = players.get(playerId);
        if (player == null) {
            JSObject ret = new JSObject();
            ret.put("method", "setRate");
            ret.put("result", false);
            ret.put("message", "Player not found");
            call.resolve(ret);
            return;
        }
        player.player.setPlaybackParameters(new PlaybackParameters(rate.floatValue(), player.player.getPlaybackParameters().pitch));
        JSObject ret = new JSObject();
        ret.put("method", "setRate");
        ret.put("result", true);
        ret.put("value", rate);
        call.resolve(ret);
    }

    public void remove(PluginCall call, String playerId) {
        MediaPlayerFragment player = players.get(playerId);
        if (player == null) {
            JSObject ret = new JSObject();
            ret.put("method", "remove");
            ret.put("result", false);
            ret.put("message", "Player not found");
            call.resolve(ret);
            return;
        }

        bridge.getActivity()
                .runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                FragmentHelpers fragmentHelpers = new FragmentHelpers(bridge);
                                ((ViewGroup) bridge.getWebView().getParent()).removeView(player.layout);
                                fragmentHelpers.removeFragment(player);
                                players.remove(playerId, player);

                                HashMap<String, Object> info = new HashMap<String, Object>();
                                info.put("playerId", playerId);
                                NotificationHelpers.defaultCenter().postNotification("MediaPlayer:Removed", info);
                                JSObject ret = new JSObject();
                                ret.put("method", "remove");
                                ret.put("result", true);
                                ret.put("value", playerId);
                                call.resolve(ret);
                            }
                        });
    }

    public void removeAll(PluginCall call) {
        bridge.getActivity()
                .runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                FragmentHelpers fragmentHelpers = new FragmentHelpers(bridge);
                                players.forEach((playerId, player) -> {
                                    ((ViewGroup) bridge.getWebView().getParent()).removeView(player.layout);
                                    fragmentHelpers.removeFragment(player);
                                    HashMap<String, Object> info = new HashMap<String, Object>();
                                    info.put("playerId", playerId);
                                    NotificationHelpers.defaultCenter().postNotification("MediaPlayer:Removed", info);
                                });
                                players.clear();
                                JSObject ret = new JSObject();
                                ret.put("method", "removeAll");
                                ret.put("result", true);
                                ret.put("value", "[]");
                                call.resolve(ret);
                            }
                        });
    }
}
