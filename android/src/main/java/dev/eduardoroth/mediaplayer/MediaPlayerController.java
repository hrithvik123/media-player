package dev.eduardoroth.mediaplayer;

import static android.app.Notification.BADGE_ICON_LARGE;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.media.MediaCodec;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.media3.cast.CastPlayer;
import androidx.media3.cast.SessionAvailabilityListener;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.trackselection.AdaptiveTrackSelection;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter;
import androidx.media3.session.MediaSession;
import androidx.media3.ui.PlayerNotificationManager;
import androidx.mediarouter.media.MediaControlIntent;
import androidx.mediarouter.media.MediaRouteSelector;
import androidx.mediarouter.media.MediaRouter;

import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastState;
import com.google.android.gms.cast.framework.CastStateListener;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import dev.eduardoroth.mediaplayer.MediaPlayerNotificationCenter.NOTIFICATION_TYPE;
import dev.eduardoroth.mediaplayer.models.AndroidOptions;
import dev.eduardoroth.mediaplayer.models.ExtraOptions;
import dev.eduardoroth.mediaplayer.models.MediaItem;
import dev.eduardoroth.mediaplayer.models.MediaPlayerNotification;
import dev.eduardoroth.mediaplayer.state.MediaPlayerState;
import dev.eduardoroth.mediaplayer.state.MediaPlayerState.UI_STATE;
import dev.eduardoroth.mediaplayer.state.MediaPlayerStateProvider;

@UnstableApi
public class MediaPlayerController {
    private final int _layoutId;
    private final String _playerId;
    private final ExtraOptions _extra;
    private final Map<String, MediaItem> _mediaItems = new HashMap<>();

    private final Context _context;
    private CastContext _castContext = null;
    private final ExoPlayer _exoPlayer;
    private final CastPlayer _castPlayer;
    private final MediaPlayerState _mediaPlayerState;
    private MediaSession _exoPlayerMediaSession;
    private MediaSession _castPlayerMediaSession;
    private PlayerNotificationManager _playerNotificationManager;

    private final Handler _handlerCurrentTime = new Handler();

    private Player _activePlayer;

    public MediaPlayerController(Context context, String playerId, AndroidOptions android, ExtraOptions extra) {
        _layoutId = playerId.chars().reduce(0, Integer::sum);
        _playerId = playerId;
        _extra = extra;
        _context = context;

        _mediaPlayerState = MediaPlayerStateProvider.getState(_playerId);

        createPlayerNotificationManager();

        _exoPlayer = createExoPlayer();
        _castPlayer = createCastPlayer();

        setActivePlayer();

        _mediaPlayerState.castingState.observe(state -> {
            if (state == UI_STATE.WILL_ENTER) {
                setActivePlayer(true);
            }
            if (state == UI_STATE.WILL_EXIT) {
                setActivePlayer(false);
            }
        });
    }

    public Player getActivePlayer() {
        return _activePlayer;
    }

    public void play() {
        _mediaPlayerState.showSubtitles.set(shouldShowSubtitles());
        _activePlayer.play();
    }

    public void pause() {
        _activePlayer.pause();
    }

    public void stop() {
        _activePlayer.stop();
    }

    public long getDuration() {
        return _activePlayer.getDuration();
    }

    public long getCurrentTime() {
        return _activePlayer.getCurrentPosition();
    }

    public long setCurrentTime(long time) {
        long duration = getDuration();
        long currentTime = getCurrentTime();
        long seekPosition = currentTime == C.TIME_UNSET ? 0 : Math.min(Math.max(0, time * 1000), duration == C.TIME_UNSET ? 0 : duration);
        _activePlayer.seekTo(seekPosition);
        return seekPosition;
    }

    public boolean isPlaying() {
        return _activePlayer.isPlaying();
    }

    public boolean isMuted() {
        return _activePlayer.getVolume() == 0;
    }

    public void mute() {
        _activePlayer.setVolume(0);
    }

    public float getVolume() {
        return _activePlayer.getVolume();
    }

    public void setVolume(float volume) {
        _activePlayer.setVolume(volume);
    }

    public float getRate() {
        return _activePlayer.getPlaybackParameters().speed;
    }

    public void setRate(float rate) {
        _activePlayer.setPlaybackSpeed(rate);
    }

    public void addMediaItem(MediaItem item) {
        _mediaItems.put(item.getMediaItem().mediaId, item);
        _exoPlayer.addMediaItem(item.getMediaItem());
        if (_castPlayer != null) {
            _castPlayer.addMediaItem(item.getMediaItem());
        }
    }

    public void addMediaItems(ArrayList<MediaItem> items) {
        items.forEach(this::addMediaItem);
    }

    public boolean shouldShowSubtitles() {
        androidx.media3.common.MediaItem current = _exoPlayer.getCurrentMediaItem();
        if (current != null) {
            MediaItem mediaItem = _mediaItems.get(current.mediaId);
            if (mediaItem != null) {
                return mediaItem.hasSubtitles();
            }
        }
        return false;
    }

    public void destroy() {
        _exoPlayer.pause();
        if (_exoPlayerMediaSession != null) {
            _exoPlayerMediaSession.release();
        }
        _exoPlayer.release();

        if (_mediaPlayerState.canCast.get() && _castPlayer != null) {
            _castPlayer.pause();
            _castPlayer.release();
            _castPlayerMediaSession.release();
            _castPlayerMediaSession = null;
        }

        _playerNotificationManager.setPlayer(null);
        _playerNotificationManager.invalidate();
    }

    private void setActivePlayer() {
        setActivePlayer(false);
    }

    private void setActivePlayer(boolean isCasting) {
        setActivePlayer(isCasting ? _castPlayer : _exoPlayer, isCasting);
    }

    private void setActivePlayer(Player playerToChange, boolean isCasting) {
        if (_activePlayer == playerToChange) {
            return;
        }
        long currentTime = _mediaPlayerState.getCurrentTime.get();
        if (_activePlayer != null) {
            _activePlayer.stop();
        }
        _activePlayer = playerToChange;
        _activePlayer.seekTo(currentTime);
        _playerNotificationManager.setPlayer(_activePlayer);
        _playerNotificationManager.setMediaSessionToken(isCasting ? _castPlayerMediaSession.getPlatformToken() : _exoPlayerMediaSession.getPlatformToken());
        _activePlayer.prepare();
        _mediaPlayerState.castingState.set(isCasting ? UI_STATE.ACTIVE : UI_STATE.INACTIVE);
    }

    private void createPlayerNotificationManager() {
        _playerNotificationManager = new PlayerNotificationManager.Builder(_context, _layoutId, _context.getString(R.string.channel_id)).setChannelNameResourceId(R.string.channel_name).setChannelDescriptionResourceId(R.string.channel_description).setChannelImportance(NotificationManager.IMPORTANCE_DEFAULT).setNotificationListener(new PlayerNotificationManager.NotificationListener() {
            @Override
            public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {
                PlayerNotificationManager.NotificationListener.super.onNotificationCancelled(notificationId, dismissedByUser);
            }

            @Override
            public void onNotificationPosted(int notificationId, @NonNull Notification notification, boolean ongoing) {
                PlayerNotificationManager.NotificationListener.super.onNotificationPosted(notificationId, notification, ongoing);
            }
        }).build();
        _playerNotificationManager.setBadgeIconType(BADGE_ICON_LARGE);
        _playerNotificationManager.setShowPlayButtonIfPlaybackIsSuppressed(true);
        _playerNotificationManager.setUseChronometer(true);
        _playerNotificationManager.setUseFastForwardAction(true);
        _playerNotificationManager.setUseFastForwardActionInCompactView(true);
        _playerNotificationManager.setUseNextAction(false);
        _playerNotificationManager.setUseNextActionInCompactView(false);
        _playerNotificationManager.setUsePlayPauseActions(true);
        _playerNotificationManager.setUsePreviousAction(false);
        _playerNotificationManager.setUsePreviousActionInCompactView(false);
        _playerNotificationManager.setUseRewindAction(true);
        _playerNotificationManager.setUseRewindActionInCompactView(true);
        _playerNotificationManager.setUseStopAction(true);
    }

    @OptIn(markerClass = UnstableApi.class)
    private ExoPlayer createExoPlayer() {
        ExoPlayer exoPlayer = new ExoPlayer.Builder(_context).setName(_playerId).setTrackSelector(new DefaultTrackSelector(_context, new AdaptiveTrackSelection.Factory())).setLoadControl(new DefaultLoadControl()).setBandwidthMeter(new DefaultBandwidthMeter.Builder(_context).build()).setDeviceVolumeControlEnabled(false).setSeekBackIncrementMs(MediaPlayer.VIDEO_STEP).setSeekForwardIncrementMs(MediaPlayer.VIDEO_STEP).setVideoScalingMode(MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT).build();

        exoPlayer.setRepeatMode(_extra.loopOnEnd ? Player.REPEAT_MODE_ONE : Player.REPEAT_MODE_OFF);
        exoPlayer.setAudioAttributes(new AudioAttributes.Builder().setContentType(C.AUDIO_CONTENT_TYPE_MOVIE).setAllowedCapturePolicy(C.ALLOW_CAPTURE_BY_SYSTEM).setUsage(C.USAGE_MEDIA).build(), true);

        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPositionDiscontinuity(@NonNull Player.PositionInfo oldPosition, @NonNull Player.PositionInfo newPosition, int reason) {
                Player.Listener.super.onPositionDiscontinuity(oldPosition, newPosition, reason);
                if (reason == Player.DISCONTINUITY_REASON_SEEK) {
                    MediaPlayerNotificationCenter.post(MediaPlayerNotification.create(_playerId, NOTIFICATION_TYPE.MEDIA_PLAYER_SEEK).addData("previousTime", oldPosition.positionMs / 1000).addData("newTime", newPosition.positionMs / 1000).build());
                }
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                if (isPlaying) {
                    MediaPlayerNotificationCenter.post(MediaPlayerNotification.create(_playerId, NOTIFICATION_TYPE.MEDIA_PLAYER_PLAY).build());
                    _handlerCurrentTime.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            MediaPlayerNotificationCenter.post(MediaPlayerNotification.create(_playerId, NOTIFICATION_TYPE.MEDIA_PLAYER_TIME_UPDATED).addData("currentTime", _exoPlayer.getCurrentPosition() / 1000).build());
                            _handlerCurrentTime.postDelayed(this, 100);
                        }
                    }, 100);
                } else {
                    _handlerCurrentTime.removeCallbacksAndMessages(null);
                    MediaPlayerNotificationCenter.post(MediaPlayerNotification.create(_playerId, NOTIFICATION_TYPE.MEDIA_PLAYER_PAUSE).build());
                }
            }

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Player.Listener.super.onPlaybackStateChanged(playbackState);
                switch (playbackState) {
                    case Player.STATE_BUFFERING:
                    case Player.STATE_IDLE:
                        break;
                    case Player.STATE_ENDED:
                        MediaPlayerNotificationCenter.post(MediaPlayerNotification.create(_playerId, NOTIFICATION_TYPE.MEDIA_PLAYER_ENDED).build());
                        break;
                    case Player.STATE_READY:
                        MediaPlayerNotificationCenter.post(MediaPlayerNotification.create(_playerId, NOTIFICATION_TYPE.MEDIA_PLAYER_READY).build());
                        if (_extra.autoPlayWhenReady) {
                            exoPlayer.play();
                        }
                        break;
                }
            }
        });

        _exoPlayerMediaSession = new MediaSession.Builder(_context, exoPlayer).setPeriodicPositionUpdateEnabled(true).build();

        _exoPlayerMediaSession.setPlayer(exoPlayer);
        exoPlayer.prepare();

        return exoPlayer;
    }

    @OptIn(markerClass = UnstableApi.class)
    private CastPlayer createCastPlayer() {
        try {
            _castContext = CastContext.getSharedInstance(_context, MoreExecutors.directExecutor()).getResult();
        } catch (RuntimeException ignored) {
        }
        if (_castContext == null) {
            return null;
        }

        CastPlayer castPlayer = new CastPlayer(_castContext);

        MediaRouter mRouter = MediaRouter.getInstance(_context);
        MediaRouteSelector mSelector = new MediaRouteSelector.Builder().addControlCategories(Arrays.asList(MediaControlIntent.CATEGORY_LIVE_AUDIO, MediaControlIntent.CATEGORY_LIVE_VIDEO)).build();

        mRouter.addCallback(mSelector, new MediaRouter.Callback() {
        }, MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);

        _castContext.addCastStateListener(state -> _mediaPlayerState.canCast.set(state != CastState.NO_DEVICES_AVAILABLE));

        castPlayer.addListener(new CastPlayer.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                CastPlayer.Listener.super.onPlaybackStateChanged(playbackState);
                if (playbackState == CastPlayer.STATE_READY) {
                    if (castPlayer.isPlaying()) {
                        MediaPlayerNotificationCenter.post(MediaPlayerNotification.create(_playerId, NOTIFICATION_TYPE.MEDIA_PLAYER_PLAY).build());
                    } else {
                        MediaPlayerNotificationCenter.post(MediaPlayerNotification.create(_playerId, NOTIFICATION_TYPE.MEDIA_PLAYER_PAUSE).build());
                    }
                }
            }
        });

        castPlayer.setSessionAvailabilityListener(new SessionAvailabilityListener() {
            @Override
            public void onCastSessionAvailable() {
                if (_mediaPlayerState != null) {
                    _mediaPlayerState.castingState.set(UI_STATE.WILL_ENTER);
                }
            }

            @Override
            public void onCastSessionUnavailable() {
                if (_mediaPlayerState != null) {
                    _mediaPlayerState.castingState.set(UI_STATE.WILL_EXIT);
                }
            }
        });

        _castPlayerMediaSession = new MediaSession.Builder(_context, castPlayer).setPeriodicPositionUpdateEnabled(true).build();

        _castPlayerMediaSession.setPlayer(castPlayer);

        castPlayer.prepare();

        return castPlayer;
    }

}
