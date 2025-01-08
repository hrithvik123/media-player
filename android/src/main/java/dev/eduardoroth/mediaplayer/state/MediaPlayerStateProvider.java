package dev.eduardoroth.mediaplayer.state;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.media3.common.util.UnstableApi;

import java.util.HashMap;

public class MediaPlayerStateProvider {
    private static final MediaPlayerStateProvider _provider = new MediaPlayerStateProvider();
    private final HashMap<String, MediaPlayerState> _instances = new HashMap<>();

    public static MediaPlayerState getState(String playerId) {
        if (!_provider._instances.containsKey(playerId)) {
            throw new Error("State not found for playerId " + playerId);
        }
        return _provider._instances.get(playerId);
    }

    public static MediaPlayerState createState(String playerId, @NonNull LifecycleOwner owner) {
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
