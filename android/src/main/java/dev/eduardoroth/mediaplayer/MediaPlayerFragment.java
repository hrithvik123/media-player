package dev.eduardoroth.mediaplayer;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Process.*;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.PictureInPictureParams;
import android.app.UiModeManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.MediaCodec;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Rational;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.app.PictureInPictureModeChangedInfo;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.util.Consumer;
import androidx.fragment.app.Fragment;
import androidx.media3.cast.CastPlayer;
import androidx.media3.cast.SessionAvailabilityListener;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Player;
import androidx.media3.common.util.Util;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.trackselection.AdaptiveTrackSelection;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter;
import androidx.media3.session.MediaSession;
import androidx.media3.ui.PlayerView;
import androidx.mediarouter.app.MediaRouteButton;
import androidx.mediarouter.media.MediaControlIntent;
import androidx.mediarouter.media.MediaRouteSelector;
import androidx.mediarouter.media.MediaRouter;

import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastState;
import com.google.android.gms.cast.framework.CastStateListener;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import dev.eduardoroth.mediaplayer.models.AndroidOptions;
import dev.eduardoroth.mediaplayer.models.ExtraOptions;
import dev.eduardoroth.mediaplayer.utilities.FragmentHelpers;
import dev.eduardoroth.mediaplayer.utilities.MediaHelpers;
import dev.eduardoroth.mediaplayer.utilities.NotificationHelpers;
import dev.eduardoroth.mediaplayer.utilities.SubtitlesHelpers;

@SuppressLint("UnsafeOptInUsageError")
public class MediaPlayerFragment extends Fragment {

    /**
     * State variables
     */
    private final String PLAYBACK_TIME;

    /**
     * Public variables
     */
    public final String playerId;
    public final int layoutId;

    /**
     * Properties
     */
    private final Context context;
    private final Uri url;
    private Uri videoPath;
    private final AndroidOptions android;
    private final ExtraOptions extra;

    /**
     * Player
     */
    public Player player;
    private ExoPlayer localPlayer;
    private Drawable artwork;
    private static final int VIDEO_STEP = 10000;

    /**
     * Picture in Picture
     */
    private final boolean canUsePiP;
    private Boolean isInPipMode = false;

    /**
     * Listeners
     */
    private final Handler handler = new Handler();
    private Player.Listener localListener;
    private PlayerView.FullscreenButtonClickListener fullscreenButtonClickListener;
    private View.OnKeyListener onKeyListener;
    private Consumer<PictureInPictureModeChangedInfo> onPictureInPictureModeChangedListener;
    private OnBackPressedCallback onBackPressedCallback;
    private PlayerView.ControllerVisibilityListener controllerVisibilityListener;
    private View.OnClickListener pipButtonClickListener;

    /**
     * Chromecast
     */
    private final CastContext castContext;
    private CastPlayer castPlayer;
    private CastPlayer.Listener castListener;
    private CastStateListener castStateListener;
    private SessionAvailabilityListener sessionAvailabilityListener;
    private MediaRouter mRouter;
    private final MediaRouter.Callback mCallback = new MediaRouter.Callback() {
    };
    private MediaRouteSelector mSelector;

    /**
     * Media session
     */
    private MediaSession mediaSession;
    private final SubtitlesHelpers subtitlesHelpers;

    /**
     * Helpers
     */
    private MediaHelpers mediaHelpers;
    private FragmentHelpers fragmentHelpers;

    /**
     * Views
     */
    public FrameLayout layout;
    public final FrameLayout.LayoutParams embeddedParams;
    private View mainView;
    private PlayerView playerView;
    private LinearLayout rightButtons;
    private MediaRouteButton castButton;
    private ImageButton pipButton;

    public MediaPlayerFragment(FragmentHelpers fragmentHelpers, Context context, CastContext castContext, int layoutId, String playerId, Uri url, AndroidOptions android, ExtraOptions extra) {
        this.fragmentHelpers = fragmentHelpers;
        this.context = context;
        this.castContext = castContext;
        this.playerId = playerId;
        this.layoutId = layoutId;

        this.PLAYBACK_TIME = playerId + ":CURRENT_TIME";
        this.url = url;
        this.android = android;
        this.extra = extra;

        this.canUsePiP = android.enablePiP && context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE);

        layout = new FrameLayout(context);
        layout.setId(layoutId);
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        embeddedParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        embeddedParams.height = (int) (android.height * metrics.density);
        embeddedParams.width = (int) (android.width * metrics.density);
        embeddedParams.topMargin = (int) (android.top * metrics.density);
        embeddedParams.leftMargin = (int) (android.left * metrics.density);
        layout.setLayoutParams(embeddedParams);
        layout.setBackgroundColor(context.getColor(R.color.black));

        this.subtitlesHelpers = new SubtitlesHelpers(extra.subtitles, playerView);
        this.mediaHelpers = new MediaHelpers(this.url.toString(), extra, context, playerView);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        createPlayer();
        if (android.enableChromecast) {
            setupChromeCast();
            addChromecastListeners();
        }
        if (savedInstanceState != null) {
            player.seekTo(savedInstanceState.getInt(PLAYBACK_TIME));
        }
        addActivityListeners();
        addPlayerListeners();
        setCurrentPlayer(castPlayer != null && castPlayer.isCastSessionAvailable() ? castPlayer : localPlayer);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mainView = inflater.inflate(R.layout.fragment_mediaplayer, container, false);
        createPlayerView();
        return mainView;
    }

    private void createPlayerView() {
        playerView = mainView.findViewById(R.id.videoViewId);
        ImageButton repeatToggle = playerView.findViewById(androidx.media3.ui.R.id.exo_repeat_toggle);
        repeatToggle.setVisibility(View.GONE);

        rightButtons = playerView.findViewById(R.id.right_buttons);
        castButton = rightButtons.findViewById(R.id.cast_button);
        pipButton = rightButtons.findViewById(R.id.pip_button);

        if (!android.enableChromecast) {
            castButton.setVisibility(View.GONE);
        }
        if (!canUsePiP) {
            pipButton.setVisibility(View.GONE);
        }

        playerView.setControllerAutoShow(false);
        playerView.setShowSubtitleButton(subtitlesHelpers.hasSubtitles());
        playerView.setControllerHideOnTouch(true);
        playerView.setControllerShowTimeoutMs(3500);
        if (artwork != null) {
            playerView.setDefaultArtwork(artwork);
            playerView.setArtworkDisplayMode(PlayerView.ARTWORK_DISPLAY_MODE_FILL);
        }
        playerView.setShowPreviousButton(false);
        playerView.setShowNextButton(false);
        playerView.setUseController(extra.showControls);
        mainView.setFocusableInTouchMode(true);
    }

    private void createPlayer() {
        ExoPlayer.Builder builder = new ExoPlayer
                .Builder(context)
                .setName(playerId)
                .setTrackSelector(new DefaultTrackSelector(context, new AdaptiveTrackSelection.Factory()))
                .setLoadControl(new DefaultLoadControl())
                .setBandwidthMeter(new DefaultBandwidthMeter.Builder(context).build())
                .setDeviceVolumeControlEnabled(true)
                .setSeekBackIncrementMs(10000)
                .setSeekForwardIncrementMs(10000)
                .setVideoScalingMode(MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT);

        videoPath = this.url;

        // TODO: implement internal video picker
        boolean isInternal = false;
        if (isInternal) {
            videoPath = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, 1);
        }
        if (this.url.toString().startsWith("file:///android_asset") || this.url.toString().startsWith("content://media")) {
            builder.setMediaSourceFactory(this.mediaHelpers.getAssetFactory());
        } else {
            builder.setMediaSourceFactory(this.mediaHelpers.getHttpFactory());
        }
        if (extra.poster != null) {
            try {
                artwork = Drawable.createFromStream(context.getContentResolver().openInputStream(extra.poster), extra.poster.toString());
            } catch (FileNotFoundException ignored) {
            }
        }
        localPlayer = builder.build();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void setupChromeCast() {
        if (castContext != null) {
            castPlayer = new CastPlayer(castContext);
            CastButtonFactory.setUpMediaRouteButton(context, castButton);

            Context castThemedContext = new ContextThemeWrapper(context, androidx.mediarouter.R.style.Theme_MediaRouter);
            TypedArray a = castThemedContext.obtainStyledAttributes(
                    null,
                    androidx.mediarouter.R.styleable.MediaRouteButton,
                    androidx.mediarouter.R.attr.mediaRouteButtonStyle,
                    0
            );
            Drawable drawable = a.getDrawable(androidx.mediarouter.R.styleable.MediaRouteButton_externalRouteEnabledDrawable);
            a.recycle();
            assert drawable != null;
            DrawableCompat.setTint(drawable, context.getColor(R.color.white));
            drawable.setState(castButton.getDrawableState());
            castButton.setRemoteIndicatorDrawable(drawable);
            if (castContext.getCastState() != CastState.NO_DEVICES_AVAILABLE) {
                castButton.setVisibility(View.VISIBLE);
            } else{
                castButton.setVisibility(View.GONE);
            }

            MediaRouter mRouter = MediaRouter.getInstance(context);
            MediaRouter.Callback mCallback = new MediaRouter.Callback() {
            };
            MediaRouteSelector mSelector = new MediaRouteSelector.Builder()
                    .addControlCategories(Arrays.asList(MediaControlIntent.CATEGORY_LIVE_AUDIO, MediaControlIntent.CATEGORY_LIVE_VIDEO))
                    .build();
            mRouter.addCallback(mSelector, mCallback, MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);
            setCurrentPlayer(castPlayer.isCastSessionAvailable() ? castPlayer : localPlayer);
        } else {
            setCurrentPlayer(localPlayer);
        }
    }

    private void addPlayerListeners() {
        Fragment fragment = this;
        localListener = new Player.Listener() {
            @Override
            public void onPositionDiscontinuity(@NonNull Player.PositionInfo oldPosition, @NonNull Player.PositionInfo newPosition, int reason) {
                Player.Listener.super.onPositionDiscontinuity(oldPosition, newPosition, reason);
                if (reason == Player.DISCONTINUITY_REASON_SEEK) {
                    HashMap<String, Object> info = new HashMap<String, Object>();
                    info.put("playerId", playerId);
                    info.put("previousTime", oldPosition.positionMs / 1000);
                    info.put("newTime", newPosition.positionMs / 1000);
                    NotificationHelpers.defaultCenter().postNotification("MediaPlayer:Seeked", info);
                }
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                Player.Listener.super.onIsPlayingChanged(isPlaying);
                HashMap<String, Object> info = new HashMap<String, Object>();
                info.put("playerId", playerId);
                if (isPlaying) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            HashMap<String, Object> timeUpdate = new HashMap<String, Object>();
                            timeUpdate.put("playerId", playerId);
                            timeUpdate.put("currentTime", player.getCurrentPosition() / 1000);
                            NotificationHelpers.defaultCenter().postNotification("MediaPlayer:TimeUpdate", timeUpdate);
                            handler.postDelayed(this, 100);
                        }
                    }, 100);
                    NotificationHelpers.defaultCenter().postNotification("MediaPlayer:Play", info);
                } else {
                    handler.removeCallbacksAndMessages(null);
                    NotificationHelpers.defaultCenter().postNotification("MediaPlayer:Pause", info);
                }
            }

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Player.Listener.super.onPlaybackStateChanged(playbackState);
                HashMap<String, Object> info = new HashMap<String, Object>();
                info.put("playerId", playerId);
                switch (playbackState) {
                    case Player.STATE_BUFFERING:
                    case Player.STATE_IDLE:
                        break;
                    case Player.STATE_ENDED:
                        NotificationHelpers.defaultCenter().postNotification("MediaPlayer:Ended", info);
                        break;
                    case Player.STATE_READY:
                        NotificationHelpers.defaultCenter().postNotification("MediaPlayer:Ready", info);
                        break;
                }
            }
        };
        onKeyListener = (container, keyCode, keyEvent) -> {
            if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
                long duration = player.getDuration();
                long videoPosition = player.getCurrentPosition();
                if (isDeviceTV(context)) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_RIGHT:
                            if (videoPosition < duration - VIDEO_STEP) {
                                player.seekTo(videoPosition + (long) VIDEO_STEP);
                            }
                            break;
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            if (videoPosition - VIDEO_STEP > 0) {
                                player.seekTo(videoPosition - (long) VIDEO_STEP);
                            } else {
                                player.seekTo(0);
                            }
                            break;
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                            if (player.isPlaying()) {
                                player.pause();
                            } else {
                                player.play();
                            }
                            break;
                        case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                            if (videoPosition < duration - (VIDEO_STEP * 2)) {
                                player.seekTo(videoPosition + ((long) VIDEO_STEP * 2));
                            }
                            break;
                        case KeyEvent.KEYCODE_MEDIA_REWIND:
                            if (videoPosition - (VIDEO_STEP * 2) > 0) {
                                player.seekTo(videoPosition - ((long) VIDEO_STEP * 2));
                            } else {
                                player.seekTo(0);
                            }
                            break;
                    }
                }
                return true;
            } else {
                return false;
            }
        };
        int defaultOrientation = requireActivity().getRequestedOrientation();
        fullscreenButtonClickListener = isFullScreen -> {
            HashMap<String, Object> info = new HashMap<String, Object>();
            info.put("playerId", playerId);
            info.put("isInFullScreen", isFullScreen);
            NotificationHelpers.defaultCenter().postNotification("MediaPlayer:Fullscreen", info);
            fragmentHelpers.updateFragmentLayout(fragment, isFullScreen);
            if (isFullScreen) {
                requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_USER);
                requireActivity().getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                );
            } else {
                requireActivity().setRequestedOrientation(defaultOrientation);
                requireActivity().getWindow()
                        .setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            }
        };
        controllerVisibilityListener = visibility -> {
            rightButtons.setVisibility(visibility);
        };
        pipButtonClickListener = view -> {
            startPictureInPicture();
        };

        if (canUsePiP) {
            pipButton.setOnClickListener(pipButtonClickListener);
        }

        player.addListener(localListener);
        playerView.setControllerVisibilityListener(controllerVisibilityListener);
        playerView.setOnKeyListener(onKeyListener);
        playerView.setFullscreenButtonClickListener(fullscreenButtonClickListener);
    }

    private void removePlayerListeners() {
        player.removeListener(localListener);
        playerView.setControllerVisibilityListener((PlayerView.ControllerVisibilityListener) null);
        playerView.setOnKeyListener(null);
        playerView.setFullscreenButtonClickListener(null);
        if (canUsePiP) {
            pipButton.setOnClickListener(pipButtonClickListener);
        }
    }

    private void addActivityListeners() {
        Fragment fragment = this;
        onPictureInPictureModeChangedListener = new Consumer<PictureInPictureModeChangedInfo>() {
            @Override
            public void accept(PictureInPictureModeChangedInfo info) {
                isInPipMode = info.isInPictureInPictureMode();
                HashMap<String, Object> infoPip = new HashMap<String, Object>();
                infoPip.put("playerId", playerId);
                infoPip.put("isInPictureInPicture", isInPipMode);
                NotificationHelpers.defaultCenter().postNotification("MediaPlayer:PictureInPicture", infoPip);
                if (isInPipMode) {
                    pipButton.setVisibility(View.GONE);
                    fragmentHelpers.updateFragmentLayout(fragment, true);
                    playerView.setUseController(false);
                    playerView.setControllerAutoShow(false);
                } else {
                    pipButton.setVisibility(View.VISIBLE);
                    fragmentHelpers.updateFragmentLayout(fragment, false);
                    playerView.setUseController(true);
                    playerView.setControllerAutoShow(true);
                }
            }
        };
        onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (player.isPlaying() && android.automaticallyEnterPiP) {
                    startPictureInPicture();
                }
            }
        };

        requireActivity().addOnPictureInPictureModeChangedListener(onPictureInPictureModeChangedListener);
        requireActivity().getOnBackPressedDispatcher().addCallback(onBackPressedCallback);
    }

    private void removeActivityListeners() {
        requireActivity().removeOnPictureInPictureModeChangedListener(onPictureInPictureModeChangedListener);
        onBackPressedCallback.setEnabled(false);
    }

    private void addChromecastListeners() {
        castListener = new CastPlayer.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                CastPlayer.Listener.super.onPlaybackStateChanged(playbackState);
                HashMap<String, Object> info = new HashMap<String, Object>();
                info.put("playerId", playerId);
                if (playbackState == CastPlayer.STATE_READY) {
                    if (castPlayer.isPlaying()) {
                        NotificationHelpers.defaultCenter().postNotification("MediaPlayer:Play", info);
                    } else {
                        NotificationHelpers.defaultCenter().postNotification("MediaPlayer:Pause", info);
                    }
                }
            }
        };
        castStateListener = state -> {
            if (state == CastState.NO_DEVICES_AVAILABLE) {
                castButton.setVisibility(View.GONE);
            } else {
                if (castButton.getVisibility() == View.GONE) {
                    castButton.setVisibility(View.VISIBLE);
                }
            }
        };
        sessionAvailabilityListener = new SessionAvailabilityListener() {
            @Override
            public void onCastSessionAvailable() {
                setCurrentPlayer(castPlayer);
            }

            @Override
            public void onCastSessionUnavailable() {
                setCurrentPlayer(localPlayer);
            }
        };

        if (castContext != null) {
            castContext.addCastStateListener(castStateListener);
            castContext.addCastStateListener(castStateListener);
        }
        if (castPlayer != null) {
            castPlayer.addListener(castListener);
            castPlayer.setSessionAvailabilityListener(sessionAvailabilityListener);
        }
    }

    private void removeChromecastListeners() {
        if (castContext != null) {
            castContext.removeCastStateListener(castStateListener);
            castContext.removeCastStateListener(castStateListener);
        }
        if (castPlayer != null) {
            castPlayer.removeListener(castListener);
            castPlayer.setSessionAvailabilityListener(null);
        }
    }

    private void startPictureInPicture() {
        if (!isInPipMode && canUsePiP) {
            final Rect sourceRectHint = new Rect();
            playerView.getGlobalVisibleRect(sourceRectHint);
            PictureInPictureParams.Builder pictureInPictureParams = new PictureInPictureParams
                    .Builder()
                    .setSourceRectHint(sourceRectHint)
                    .setAspectRatio(new Rational(player.getVideoSize().width, player.getVideoSize().height));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                pictureInPictureParams.setAutoEnterEnabled(android.automaticallyEnterPiP);
                pictureInPictureParams.setSeamlessResizeEnabled(true);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pictureInPictureParams.setTitle(extra.title);
                pictureInPictureParams.setSubtitle(extra.subtitle);
                pictureInPictureParams.setExpandedAspectRatio(new Rational(player.getVideoSize().width, player.getVideoSize().height));
            }

            if (extra.subtitles != null && extra.subtitles.url != null)
                subtitlesHelpers.setSubtitle(true);

            requireActivity().enterPictureInPictureMode(pictureInPictureParams.build());
            isInPipMode = requireActivity().isInPictureInPictureMode();
        }
    }

    private void setCurrentPlayer(Player currentPlayer) {
        if (this.player == currentPlayer) {
            return;
        }
        assert currentPlayer != null;

        long playbackPositionMs = C.TIME_UNSET;
        Player previousPlayer = this.player;
        if (previousPlayer != null) {
            int playbackState = previousPlayer.getPlaybackState();
            if (playbackState != Player.STATE_ENDED) {
                playbackPositionMs = previousPlayer.getCurrentPosition();
            }
            previousPlayer.stop();
        }

        currentPlayer.setVolume(1);
        player = currentPlayer;

        playerView.setPlayer(currentPlayer);

        List<MediaItem.SubtitleConfiguration> subtitlesConfig = new ArrayList<MediaItem.SubtitleConfiguration>();
        if (extra.subtitles != null && extra.subtitles.url != null) {
            subtitlesConfig.add(subtitlesHelpers.getSubtitlesFactory());
        }

        MediaMetadata.Builder movieMetadataBuilder = new MediaMetadata.Builder()
                .setTitle(extra.title)
                .setSubtitle(extra.subtitle)
                .setArtist(extra.artist)
                .setMediaType(MediaMetadata.MEDIA_TYPE_MOVIE);
        if (extra.poster != null) {
            movieMetadataBuilder.setArtworkUri(extra.poster);
        }

        currentPlayer.setAudioAttributes(
                new AudioAttributes
                        .Builder()
                        .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                        .setAllowedCapturePolicy(C.ALLOW_CAPTURE_BY_SYSTEM)
                        .setUsage(C.USAGE_MEDIA)
                        .build(),
                true);

        currentPlayer.addMediaItem(
                new MediaItem
                        .Builder()
                        .setSubtitleConfigurations(subtitlesConfig)
                        .setUri(videoPath)
                        .setMediaMetadata(movieMetadataBuilder.build())
                        .build()
        );

        currentPlayer.seekTo(playbackPositionMs);

        if (extra.loopOnEnd) {
            currentPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);
        } else {
            currentPlayer.setRepeatMode(Player.REPEAT_MODE_OFF);
        }

        if (mediaSession == null) {
            mediaSession = new MediaSession
                    .Builder(context, currentPlayer)
                    .setPeriodicPositionUpdateEnabled(true)
                    .build();
        }
        mediaSession.setPlayer(player);
        player.prepare();
    }

    private boolean isDeviceTV(Context context) {
        if (Build.VERSION.SDK_INT >= LOLLIPOP) {
            UiModeManager uiManager = (UiModeManager) context.getSystemService(Context.UI_MODE_SERVICE);
            return uiManager != null && uiManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION;
        }
        return false;
    }

    public boolean isApplicationSentToBackground() {
        int pid = myPid();
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> procInfos = am.getRunningAppProcesses();
        if (procInfos != null) {
            for (ActivityManager.RunningAppProcessInfo appProcess : procInfos) {
                if (appProcess.pid == pid) {
                    return true;
                }
            }
        }
        return false;
    }

    private void clearMediaPlayer() {
        removePlayerListeners();
        removeActivityListeners();
        removeChromecastListeners();
        if (castPlayer != null) {
            castPlayer.setSessionAvailabilityListener(null);
            castPlayer.release();
            castPlayer = null;
        }
        localPlayer.release();
        localPlayer = null;
        mediaSession.release();
        mediaSession = null;
        playerView.setPlayer(null);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (player != null) {
            outState.putInt(PLAYBACK_TIME, (int) player.getCurrentPosition());
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT >= 24) {
            if (playerView != null) {
                // If cast is playing then it doesn't start the local player once get backs from background
                if (castContext != null && android.enableChromecast && castPlayer.isCastSessionAvailable()) {
                    return;
                }
                if (player.getCurrentPosition() != 0) {
                    player.play();
                }
            } else {
                getActivity().finishAndRemoveTask();
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        boolean isAppBackground = false;
        if (android.enableBackgroundPlay) {
            isAppBackground = isApplicationSentToBackground();
        }
        if (isInPipMode) {
            requireActivity().finishAndRemoveTask();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (android.enableChromecast) {
            //castContext.removeCastStateListener(castStateListener);
        }
        boolean isAppBackground = false;
        if (android.enableBackgroundPlay) {
            isAppBackground = isApplicationSentToBackground();
        }

        if (!isInPipMode) {
            if (Util.SDK_INT < 24) {
                if (player != null) player.setPlayWhenReady(false);
            } else {
                if (isAppBackground) {
                    if (player != null) {
                        if (player.isPlaying()) {
                            player.play();
                        }
                    }
                } else {
                    player.pause();
                }
            }
        } else {
            if (isAppBackground && player != null) {
                player.play();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //if (chromecast && castContext != null) castContext.addCastStateListener(castStateListener);
        if (!isInPipMode) {
            /*if ((Util.SDK_INT < 24 || player == null)) {
                initializePlayer();
            }*/
        } else {
            isInPipMode = false;
            if (
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
            ) {
                if (!extra.showControls) {
                    playerView.setUseController(false);
                } else {
                    playerView.setUseController(true);
                }
            }
            if (subtitlesHelpers.hasSubtitles())
                subtitlesHelpers.setSubtitle(false);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        clearMediaPlayer();
    }

}
