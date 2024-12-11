package dev.eduardoroth.mediaplayer.state;

import androidx.lifecycle.LifecycleOwner;
import androidx.media3.common.util.UnstableApi;

import java.util.HashMap;

@UnstableApi
public class MediaPlayerStateProvider {
    private static final MediaPlayerStateProvider _provider = new MediaPlayerStateProvider();
    private final HashMap<String, MediaPlayerState> _instances = new HashMap<>();

    private MediaPlayerStateProvider() {
    }

    public static MediaPlayerState getState(String playerId) {
        if (!_provider._instances.containsKey(playerId)) {
            return null;
        }
        return _provider._instances.get(playerId);
    }

    public static MediaPlayerState getState(String playerId, LifecycleOwner owner) {
        if (!_provider._instances.containsKey(playerId)) {
            MediaPlayerState playerState = new MediaPlayerState(owner);
            _provider._instances.put(playerId, playerState);
        }
        return MediaPlayerStateProvider.getState(playerId);
    }

    public static void clearState(String playerId) {
        _provider._instances.remove(playerId);
    }
}
