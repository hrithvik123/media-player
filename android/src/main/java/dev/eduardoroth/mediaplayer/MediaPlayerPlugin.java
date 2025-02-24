package dev.eduardoroth.mediaplayer;

import android.util.DisplayMetrics;
import android.util.Rational;
import android.view.ViewGroup;
import androidx.fragment.app.FragmentContainerView;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import dev.eduardoroth.mediaplayer.models.AndroidOptions;
import dev.eduardoroth.mediaplayer.models.ExtraOptions;
import dev.eduardoroth.mediaplayer.models.PlacementOptions;
import dev.eduardoroth.mediaplayer.models.SubtitleOptions;
import java.util.Objects;
import org.json.JSONException;

@CapacitorPlugin(name = "MediaPlayer")
public class MediaPlayerPlugin extends Plugin {

    private MediaPlayer implementation;

    @Override
    public void load() {
        implementation = new MediaPlayer(bridge.getActivity());
        MediaPlayerNotificationCenter.init(bridge.getActivity());
        MediaPlayerNotificationCenter.listenNotifications(nextNotification ->
            notifyListeners(nextNotification.getEventName(), nextNotification.getData())
        );

        ViewGroup coordinatorLayout = (ViewGroup) bridge.getActivity().findViewById(R.id.webview).getParent();
        FragmentContainerView fragmentContainerView = new FragmentContainerView(bridge.getContext());
        fragmentContainerView.setId(R.id.MediaPlayerFragmentContainerView);
        coordinatorLayout.addView(fragmentContainerView);
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

        JSObject placementOptions = call.getObject("placement");
        JSObject androidOptions = call.getObject("android");
        JSObject extraOptions = call.getObject("extra");
        JSObject subtitleOptions = extraOptions != null ? extraOptions.getJSObject("subtitles") : null;

        DisplayMetrics metrics = bridge.getContext().getResources().getDisplayMetrics();

        String videoOrientation = placementOptions != null ? placementOptions.getString("videoOrientation", "HORIZONTAL") : "HORIZONTAL";
        String horizontalAlignment = placementOptions != null ? placementOptions.getString("horizontalAlignment", "CENTER") : "CENTER";
        String verticalAlignment = placementOptions != null ? placementOptions.getString("verticalAlignment", "TOP") : "TOP";

        Integer paramHorizontalMargin = placementOptions != null ? placementOptions.getInteger("horizontalMargin", null) : null;
        Integer paramVerticalMargin = placementOptions != null ? placementOptions.getInteger("verticalMargin", null) : null;

        int horizontalMargin = paramHorizontalMargin != null ? (int) (paramHorizontalMargin * metrics.density) : 0;
        int verticalMargin = paramVerticalMargin != null ? (int) (paramVerticalMargin * metrics.density) : 0;

        Integer paramHeight = placementOptions != null ? placementOptions.getInteger("height", null) : null;
        Integer paramWidth = placementOptions != null ? placementOptions.getInteger("width", null) : null;

        int height;
        int width;

        if (paramHeight != null && paramWidth != null) {
            width = (int) (paramWidth * metrics.density);
            height = (int) (paramHeight * metrics.density);
        } else if (paramHeight == null && paramWidth == null) {
            if (Objects.equals(videoOrientation, "HORIZONTAL")) {
                width = metrics.widthPixels;
                height = (int) (width * (new Rational(9, 16).floatValue()));
            } else {
                height = metrics.heightPixels;
                width = (int) (height * (new Rational(9, 16).floatValue()));
            }
        } else if (paramHeight != null) {
            height = (int) (paramHeight * metrics.density);
            if (Objects.equals(videoOrientation, "HORIZONTAL")) {
                width = (int) (paramHeight * metrics.density * (new Rational(16, 9).floatValue()));
            } else {
                width = (int) (paramHeight * metrics.density * (new Rational(9, 16).floatValue()));
            }
        } else {
            width = (int) (paramWidth * metrics.density);
            if (Objects.equals(videoOrientation, "HORIZONTAL")) {
                height = (int) (width * (new Rational(16, 9).floatValue()));
            } else {
                height = (int) (width * (new Rational(9, 16).floatValue()));
            }
        }

        if (width + horizontalMargin > metrics.widthPixels) {
            width = metrics.widthPixels - horizontalMargin;
            if (Objects.equals(videoOrientation, "HORIZONTAL")) {
                height = (int) (width * (new Rational(9, 16).floatValue()));
            } else {
                height = (int) (width * (new Rational(16, 9).floatValue()));
            }
        }
        if (height + verticalMargin > metrics.heightPixels) {
            height = metrics.heightPixels - verticalMargin;
            if (Objects.equals(videoOrientation, "HORIZONTAL")) {
                width = (int) (height * (new Rational(16, 9).floatValue()));
            } else {
                width = (int) (height * (new Rational(9, 16).floatValue()));
            }
        }

        PlacementOptions placement = new PlacementOptions(
            height,
            width,
            videoOrientation,
            horizontalAlignment,
            verticalAlignment,
            horizontalMargin,
            verticalMargin
        );

        AndroidOptions android = new AndroidOptions(
            androidOptions == null || androidOptions.optBoolean("enableChromecast", true),
            androidOptions == null || androidOptions.optBoolean("enablePiP", true),
            androidOptions == null || androidOptions.optBoolean("enableBackgroundPlay", true),
            androidOptions != null && androidOptions.optBoolean("openInFullscreen", false),
            androidOptions != null && androidOptions.optBoolean("automaticallyEnterPiP", false),
            androidOptions == null || androidOptions.optBoolean("fullscreenOnLandscape", true),
            androidOptions == null || androidOptions.optBoolean("stopOnTaskRemoved", false)
        );

        SubtitleOptions subtitles = null;
        if (subtitleOptions != null) {
            double fontSize = Double.parseDouble("12");
            try {
                fontSize = subtitleOptions.getDouble("fontSize");
            } catch (NullPointerException | JSONException ignored) {}
            subtitles = new SubtitleOptions(
                subtitleOptions.getString("url", null),
                subtitleOptions.getString("language", "English"),
                subtitleOptions.getString("foregroundColor", null),
                subtitleOptions.getString("backgroundColor", null),
                fontSize
            );
        }

        double rate = 1;
        try {
            rate = extraOptions.getDouble("rate");
        } catch (NullPointerException | JSONException ignored) {}

        ExtraOptions extra = new ExtraOptions(
            extraOptions != null ? extraOptions.getString("title") : null,
            extraOptions != null ? extraOptions.getString("subtitle") : null,
            extraOptions != null ? extraOptions.getString("poster", null) : null,
            extraOptions != null ? extraOptions.getString("artist", null) : null,
            rate,
            subtitles,
            extraOptions != null && extraOptions.optBoolean("autoPlayWhenReady", false),
            extraOptions != null && extraOptions.optBoolean("loopOnEnd", false),
            extraOptions == null || extraOptions.optBoolean("showControls", true),
            extraOptions != null ? extraOptions.getJSObject("headers") : null
        );
        bridge.getActivity().runOnUiThread(() -> implementation.create(call, playerId, url, placement, android, extra));
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
        bridge.getActivity().runOnUiThread(() -> implementation.play(call, playerId));
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
        bridge.getActivity().runOnUiThread(() -> implementation.pause(call, playerId));
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
        bridge.getActivity().runOnUiThread(() -> implementation.getDuration(call, playerId));
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
        bridge.getActivity().runOnUiThread(() -> implementation.getCurrentTime(call, playerId));
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
        bridge.getActivity().runOnUiThread(() -> implementation.setCurrentTime(call, playerId, time.longValue()));
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
        bridge.getActivity().runOnUiThread(() -> implementation.isPlaying(call, playerId));
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
        bridge.getActivity().runOnUiThread(() -> implementation.isMuted(call, playerId));
    }

    @PluginMethod
    public void setVisibilityBackgroundForPiP(final PluginCall call) {
        JSObject ret = new JSObject();
        ret.put("method", "setVisibilityBackgroundForPiP");
        ret.put("result", false);
        ret.put("message", "Method setVisibilityBackgroundForPiP not implemented for Android");
        call.resolve(ret);
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
        bridge.getActivity().runOnUiThread(() -> implementation.mute(call, playerId));
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
        bridge.getActivity().runOnUiThread(() -> implementation.getVolume(call, playerId));
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
        bridge.getActivity().runOnUiThread(() -> implementation.setVolume(call, playerId, volume));
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
        bridge.getActivity().runOnUiThread(() -> implementation.getRate(call, playerId));
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
        bridge.getActivity().runOnUiThread(() -> implementation.setRate(call, playerId, rate));
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
        bridge.getActivity().runOnUiThread(() -> implementation.remove(call, playerId));
    }

    @PluginMethod
    public void removeAll(final PluginCall call) {
        bridge.getActivity().runOnUiThread(() -> implementation.removeAll(call));
    }

    @PluginMethod
    public void isFullScreen(final PluginCall call) {
        String playerId = call.getString("playerId");
        if (playerId == null) {
            JSObject ret = new JSObject();
            ret.put("method", "isFullScreen");
            ret.put("result", false);
            ret.put("message", "Must provide a PlayerId");
            call.resolve(ret);
            return;
        }
        bridge.getActivity().runOnUiThread(() -> implementation.isFullScreen(call, playerId));
    }

    @PluginMethod
    public void toggleFullScreen(final PluginCall call) {
        String playerId = call.getString("playerId");
        if (playerId == null) {
            JSObject ret = new JSObject();
            ret.put("method", "toggleFullScreen");
            ret.put("result", false);
            ret.put("message", "Must provide a PlayerId");
            call.resolve(ret);
            return;
        }
        bridge.getActivity().runOnUiThread(() -> implementation.toggleFullScreen(call, playerId));
    }
}
