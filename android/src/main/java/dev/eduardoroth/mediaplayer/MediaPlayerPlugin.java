package dev.eduardoroth.mediaplayer;

/*
OK Ready
OK PLay
OK Pause
OK Ended

Fullscreen
PictureInPicture

Removed

OK Seeked

TimeUpdate

 */

import android.Manifest;
import android.content.Context;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.google.android.gms.cast.framework.CastContext;
import com.google.common.util.concurrent.MoreExecutors;

import android.net.Uri;
import android.util.DisplayMetrics;

import org.json.JSONException;

import dev.eduardoroth.mediaplayer.models.AndroidOptions;
import dev.eduardoroth.mediaplayer.models.ExtraOptions;
import dev.eduardoroth.mediaplayer.models.SubtitleOptions;
import dev.eduardoroth.mediaplayer.models.SubtitleSettings;
import dev.eduardoroth.mediaplayer.utilities.FileHelpers;
import dev.eduardoroth.mediaplayer.utilities.NotificationHelpers;
import dev.eduardoroth.mediaplayer.utilities.RunnableHelper;

@CapacitorPlugin(
        name = "MediaPlayer",
        permissions = {
                @Permission(alias = "mediaVideo",
                        strings = {Manifest.permission.READ_MEDIA_VIDEO}),
                @Permission(alias = "publicStorage",
                        strings = {Manifest.permission.READ_EXTERNAL_STORAGE})
        })
public class MediaPlayerPlugin extends Plugin {
    private static final String TAG = "MediaPlayer";
    private Context context;
    private CastContext castContext;
    private MediaPlayer implementation;
    private FileHelpers fileHelpers;

    @Override
    public void load() {
        context = getContext();
        try {
            castContext = CastContext.getSharedInstance(context, MoreExecutors.directExecutor()).getResult();
        } catch (RuntimeException ignored) {
        }
        addObserversToNotificationCenter();
        implementation = new MediaPlayer(context, castContext, getBridge());
        this.fileHelpers = new FileHelpers(context);
    }

    @PluginMethod
    public void create(final PluginCall call) {
        String playerId = call.getString("playerId");
        if (playerId == null) {
            JSObject ret = new JSObject();
            ret.put("method", "create");
            ret.put("result", false);
            ret.put("message", "Must provide a PlayerId");
            call.resolve(ret);
            return;
        }
        String url = call.getString("url");
        if (url == null) {
            JSObject ret = new JSObject();
            ret.put("method", "create");
            ret.put("result", false);
            ret.put("message", "Must provide a URL");
            call.resolve(ret);
            return;
        }

        Uri parsedUrl = null;
        try {
            String path = fileHelpers.getFilePath(url);
            parsedUrl = Uri.parse(path);
        } catch (NullPointerException ignored) {
        }

        JSObject androidOptions = call.getObject("android");
        JSObject extraOptions = call.getObject("extra");
        JSObject subtitleOptions = extraOptions.getJSObject("subtitles");

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();

        double top = Double.parseDouble("0");
        try {
            top = androidOptions.getDouble("top");
        } catch (JSONException ignored) {
        }
        double left = Double.parseDouble("0");
        try {
            left = androidOptions.getDouble("left");
        } catch (JSONException ignored) {
        }
        double width = Double.parseDouble(String.valueOf(metrics.widthPixels));
        try {
            width = androidOptions.getDouble("width");
        } catch (JSONException ignored) {
        }
        double height = ((double) 9 / 16) * ((double) metrics.heightPixels);
        try {
            height = androidOptions.getDouble("height");
        } catch (JSONException ignored) {
        }

        AndroidOptions android = new AndroidOptions(
                androidOptions.getBoolean("enableChromecast", true),
                androidOptions.getBoolean("enablePiP", true),
                androidOptions.getBoolean("enableBackgroundPlay", true),
                androidOptions.getBoolean("openInFullscreen", false),
                androidOptions.getBoolean("automaticallyEnterPiP", false),
                top,
                left,
                height,
                width
        );

        SubtitleOptions subtitles = null;
        if (subtitleOptions != null) {
            double fontSize = Double.parseDouble("12");
            try {
                fontSize = subtitleOptions.getDouble("fontSize");
            } catch (JSONException ignored) {
            }
            SubtitleSettings subtitleSettings = new SubtitleSettings(
                    subtitleOptions.getString("language"),
                    subtitleOptions.getString("foregroundColor"),
                    subtitleOptions.getString("backgroundColor"),
                    fontSize
            );
            Uri subtitlesUrl = null;
            try {
                subtitlesUrl = Uri.parse(subtitleOptions.getString("url"));
            } catch (NullPointerException ignored) {
            }

            subtitles = new SubtitleOptions(
                    subtitlesUrl,
                    subtitleSettings
            );
        }

        double rate = Double.parseDouble("1");
        try {
            rate = extraOptions.getDouble("rate");
        } catch (JSONException ignored) {
        }

        Uri posterUrl = null;
        try {
            posterUrl = Uri.parse(extraOptions.getString("poster"));
        } catch (NullPointerException ignored) {
        }

        ExtraOptions extra = new ExtraOptions(
                extraOptions.getString("title"),
                extraOptions.getString("subtitle"),
                posterUrl,
                extraOptions.getString("artist"),
                rate,
                subtitles,
                extraOptions.getBoolean("loopOnEnd", false),
                extraOptions.getBoolean("showControls", true),
                extraOptions.getJSObject("headers")
        );

        implementation.create(call, playerId, parsedUrl, android, extra);
    }

    @PluginMethod
    public void play(final PluginCall call) {
        String playerId = call.getString("playerId");
        if (playerId == null) {
            JSObject ret = new JSObject();
            ret.put("method", "play");
            ret.put("result", false);
            ret.put("message", "Must provide a PlayerId");
            call.resolve(ret);
            return;
        }
        bridge.getActivity().runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        implementation.play(call, playerId);
                    }
                }
        );
    }

    @PluginMethod
    public void pause(final PluginCall call) {
        String playerId = call.getString("playerId");
        if (playerId == null) {
            JSObject ret = new JSObject();
            ret.put("method", "pause");
            ret.put("result", false);
            ret.put("message", "Must provide a PlayerId");
            call.resolve(ret);
            return;
        }
        bridge.getActivity().runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        implementation.pause(call, playerId);
                    }
                }
        );
    }

    @PluginMethod
    public void getDuration(final PluginCall call) {
        String playerId = call.getString("playerId");
        if (playerId == null) {
            JSObject ret = new JSObject();
            ret.put("method", "getDuration");
            ret.put("result", false);
            ret.put("message", "Must provide a PlayerId");
            call.resolve(ret);
            return;
        }
        bridge.getActivity().runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        implementation.getDuration(call, playerId);
                    }
                }
        );
    }

    @PluginMethod
    public void getCurrentTime(final PluginCall call) {
        String playerId = call.getString("playerId");
        if (playerId == null) {
            JSObject ret = new JSObject();
            ret.put("method", "getCurrentTime");
            ret.put("result", false);
            ret.put("message", "Must provide a PlayerId");
            call.resolve(ret);
            return;
        }
        bridge.getActivity().runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        implementation.getCurrentTime(call, playerId);
                    }
                }
        );
    }

    @PluginMethod
    public void setCurrentTime(final PluginCall call) {
        String playerId = call.getString("playerId");
        Double time = call.getDouble("time");
        if (playerId == null) {
            JSObject ret = new JSObject();
            ret.put("method", "setCurrentTime");
            ret.put("result", false);
            ret.put("message", "Must provide a PlayerId");
            call.resolve(ret);
            return;
        }
        if (time == null) {
            JSObject ret = new JSObject();
            ret.put("method", "setCurrentTime");
            ret.put("result", false);
            ret.put("message", "Must provide a time");
            call.resolve(ret);
            return;
        }
        bridge.getActivity().runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        implementation.setCurrentTime(call, playerId, time);
                    }
                }
        );
    }

    @PluginMethod
    public void isPlaying(final PluginCall call) {
        String playerId = call.getString("playerId");
        if (playerId == null) {
            JSObject ret = new JSObject();
            ret.put("method", "isPlaying");
            ret.put("result", false);
            ret.put("message", "Must provide a PlayerId");
            call.resolve(ret);
            return;
        }
        bridge.getActivity().runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        implementation.isPlaying(call, playerId);
                    }
                }
        );
    }

    @PluginMethod
    public void isMuted(final PluginCall call) {
        String playerId = call.getString("playerId");
        if (playerId == null) {
            JSObject ret = new JSObject();
            ret.put("method", "isMuted");
            ret.put("result", false);
            ret.put("message", "Must provide a PlayerId");
            call.resolve(ret);
            return;
        }
        bridge.getActivity().runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        implementation.isMuted(call, playerId);
                    }
                }
        );
    }

    @PluginMethod
    public void mute(final PluginCall call) {
        String playerId = call.getString("playerId");
        if (playerId == null) {
            JSObject ret = new JSObject();
            ret.put("method", "mute");
            ret.put("result", false);
            ret.put("message", "Must provide a PlayerId");
            call.resolve(ret);
            return;
        }
        bridge.getActivity().runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        implementation.mute(call, playerId);
                    }
                }
        );
    }

    @PluginMethod
    public void getVolume(final PluginCall call) {
        String playerId = call.getString("playerId");
        if (playerId == null) {
            JSObject ret = new JSObject();
            ret.put("method", "getVolume");
            ret.put("result", false);
            ret.put("message", "Must provide a PlayerId");
            call.resolve(ret);
            return;
        }
        bridge.getActivity().runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        implementation.getVolume(call, playerId);
                    }
                }
        );
    }

    @PluginMethod
    public void setVolume(final PluginCall call) {
        String playerId = call.getString("playerId");
        Double volume = call.getDouble("volume");
        if (playerId == null) {
            JSObject ret = new JSObject();
            ret.put("method", "setVolume");
            ret.put("result", false);
            ret.put("message", "Must provide a PlayerId");
            call.resolve(ret);
            return;
        }
        if (volume == null) {
            JSObject ret = new JSObject();
            ret.put("method", "setVolume");
            ret.put("result", false);
            ret.put("message", "Must provide a volume");
            call.resolve(ret);
            return;
        }
        bridge.getActivity().runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        implementation.setVolume(call, playerId, volume);
                    }
                }
        );
    }

    @PluginMethod
    public void getRate(final PluginCall call) {
        String playerId = call.getString("playerId");
        if (playerId == null) {
            JSObject ret = new JSObject();
            ret.put("method", "getRate");
            ret.put("result", false);
            ret.put("message", "Must provide a PlayerId");
            call.resolve(ret);
            return;
        }
        bridge.getActivity().runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        implementation.getRate(call, playerId);
                    }
                }
        );
    }

    @PluginMethod
    public void setRate(final PluginCall call) {
        String playerId = call.getString("playerId");
        Double rate = call.getDouble("rate");
        if (playerId == null) {
            JSObject ret = new JSObject();
            ret.put("method", "setRate");
            ret.put("result", false);
            ret.put("message", "Must provide a PlayerId");
            call.resolve(ret);
            return;
        }
        if (rate == null) {
            JSObject ret = new JSObject();
            ret.put("method", "setRate");
            ret.put("result", false);
            ret.put("message", "Must provide a rate");
            call.resolve(ret);
            return;
        }
        bridge.getActivity().runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        implementation.setRate(call, playerId, rate);
                    }
                }
        );
    }

    @PluginMethod
    public void remove(final PluginCall call) {
        String playerId = call.getString("playerId");
        if (playerId == null) {
            JSObject ret = new JSObject();
            ret.put("method", "remove");
            ret.put("result", false);
            ret.put("message", "Must provide a PlayerId");
            call.resolve(ret);
            return;
        }
        bridge.getActivity().runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        implementation.remove(call, playerId);
                    }
                }
        );
    }

    @PluginMethod
    public void removeAll(final PluginCall call) {
        bridge.getActivity().runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        implementation.removeAll(call);
                    }
                }
        );
    }

    private void addObserversToNotificationCenter() {
        NotificationHelpers.defaultCenter()
                .addMethodForNotification("MediaPlayer:Ready", new RunnableHelper() {
                    @Override
                    public void run() {
                        JSObject data = new JSObject();
                        data.put("playerId", this.getInfo().get("playerId"));
                        notifyListeners("MediaPlayer:Ready", data);
                    }
                });
        NotificationHelpers.defaultCenter()
                .addMethodForNotification("MediaPlayer:Play", new RunnableHelper() {
                    @Override
                    public void run() {
                        JSObject data = new JSObject();
                        data.put("playerId", this.getInfo().get("playerId"));
                        notifyListeners("MediaPlayer:Play", data);
                    }
                });
        NotificationHelpers.defaultCenter()
                .addMethodForNotification("MediaPlayer:Pause", new RunnableHelper() {
                    @Override
                    public void run() {
                        JSObject data = new JSObject();
                        data.put("playerId", this.getInfo().get("playerId"));
                        notifyListeners("MediaPlayer:Pause", data);
                    }
                });
        NotificationHelpers.defaultCenter()
                .addMethodForNotification("MediaPlayer:Ended", new RunnableHelper() {
                    @Override
                    public void run() {
                        JSObject data = new JSObject();
                        data.put("playerId", this.getInfo().get("playerId"));
                        notifyListeners("MediaPlayer:Ended", data);
                    }
                });
        NotificationHelpers.defaultCenter()
                .addMethodForNotification("MediaPlayer:Removed", new RunnableHelper() {
                    @Override
                    public void run() {
                        JSObject data = new JSObject();
                        data.put("playerId", this.getInfo().get("playerId"));
                        notifyListeners("MediaPlayer:Removed", data);
                    }
                });
        NotificationHelpers.defaultCenter()
                .addMethodForNotification("MediaPlayer:Seeked", new RunnableHelper() {
                    @Override
                    public void run() {
                        JSObject data = new JSObject();
                        data.put("playerId", this.getInfo().get("playerId"));
                        data.put("previousTime", this.getInfo().get("previousTime"));
                        data.put("newTime", this.getInfo().get("newTime"));
                        notifyListeners("MediaPlayer:Seeked", data);
                    }
                });
        NotificationHelpers.defaultCenter()
                .addMethodForNotification("MediaPlayer:TimeUpdate", new RunnableHelper() {
                    @Override
                    public void run() {
                        JSObject data = new JSObject();
                        data.put("playerId", this.getInfo().get("playerId"));
                        data.put("currentTime", this.getInfo().get("currentTime"));
                        notifyListeners("MediaPlayer:TimeUpdate", data);
                    }
                });
        NotificationHelpers.defaultCenter()
                .addMethodForNotification("MediaPlayer:FullScreen", new RunnableHelper() {
                    @Override
                    public void run() {
                        JSObject data = new JSObject();
                        data.put("playerId", this.getInfo().get("playerId"));
                        data.put("isInFullScreen", this.getInfo().get("isInFullScreen"));
                        notifyListeners("MediaPlayer:FullScreen", data);
                    }
                });
        NotificationHelpers.defaultCenter()
                .addMethodForNotification("MediaPlayer:PictureInPicture", new RunnableHelper() {
                    @Override
                    public void run() {
                        JSObject data = new JSObject();
                        data.put("playerId", this.getInfo().get("playerId"));
                        data.put("isInPictureInPicture", this.getInfo().get("isInPictureInPicture"));
                        notifyListeners("MediaPlayer:PictureInPicture", data);
                    }
                });
    }

}
