package dev.eduardoroth.mediaplayer;

import static dev.eduardoroth.mediaplayer.state.MediaPlayerState.*;

import android.app.PictureInPictureParams;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Rational;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.media3.common.util.UnstableApi;

import dev.eduardoroth.mediaplayer.MediaPlayerControllerView.MEDIA_PLAYER_VIEW_TYPE;
import dev.eduardoroth.mediaplayer.models.AndroidOptions;
import dev.eduardoroth.mediaplayer.models.ExtraOptions;
import dev.eduardoroth.mediaplayer.models.MediaItem;
import dev.eduardoroth.mediaplayer.models.MediaPlayerNotification;
import dev.eduardoroth.mediaplayer.state.MediaPlayerState;
import dev.eduardoroth.mediaplayer.state.MediaPlayerStateProvider;

@UnstableApi
public class MediaPlayerContainer extends Fragment {

    private final AndroidOptions _android;
    private final ExtraOptions _extra;
    private final MediaPlayerState _mediaPlayerState;
    private MediaPlayerController _playerController;
    private MediaPlayerControllerView _playerControllerEmbeddedView;
    private MediaPlayerControllerView _playerControllerFullscreenView;
    private MediaPlayerControllerView _playerControllerPictureInPictureView;
    private final String _url;
    private final String _playerId;
    private final Rect _sourceRectHint = new Rect();

    public MediaPlayerContainer(AppCompatActivity activity, String url, String playerId, AndroidOptions android, ExtraOptions extra) {
        _android = android;
        _extra = extra;
        _playerId = playerId;
        _url = url;
        _mediaPlayerState = MediaPlayerStateProvider.getState(playerId, activity);

        _mediaPlayerState.androidOptions.set(android);
        _mediaPlayerState.extraOptions.set(extra);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        _mediaPlayerState.canUsePiP.set(_android.enablePiP && requireContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE));
        _mediaPlayerState.isPlayerReady.observe(state -> {
            if (state) {
                view.findViewById(R.id.MediaPlayerEmbeddedLoading).setVisibility(View.GONE);
            }
        });

        requireActivity().addOnPictureInPictureModeChangedListener(state -> {
            if (getLifecycle().getCurrentState() == Lifecycle.State.CREATED) {
                _mediaPlayerState.fullscreenState.set(UI_STATE.WILL_EXIT);
                _mediaPlayerState.pipState.set(UI_STATE.WILL_EXIT);
                if (!_android.enableBackgroundPlay) {
                    _playerController.getActivePlayer().pause();
                }
            } else if (getLifecycle().getCurrentState() == Lifecycle.State.STARTED) {
                if (state.isInPictureInPictureMode()) {
                    if (_mediaPlayerState.fullscreenState.get() != UI_STATE.ACTIVE) {
                        _mediaPlayerState.fullscreenState.set(UI_STATE.WILL_ENTER);
                    }
                } else {
                    _mediaPlayerState.fullscreenState.set(UI_STATE.WILL_EXIT);
                    _mediaPlayerState.pipState.set(UI_STATE.WILL_EXIT);
                }
            }

        });

        _mediaPlayerState.pipState.observe(state -> {
            switch (state) {
                case ACTIVE -> {
                    MediaPlayerNotificationCenter.post(MediaPlayerNotification.create(_playerId, MediaPlayerNotificationCenter.NOTIFICATION_TYPE.MEDIA_PLAYER_PIP).addData("isInPictureInPicture", true).build());
                }
                case INACTIVE -> {
                    MediaPlayerNotificationCenter.post(MediaPlayerNotification.create(_playerId, MediaPlayerNotificationCenter.NOTIFICATION_TYPE.MEDIA_PLAYER_PIP).addData("isInPictureInPicture", false).build());
                }
                case WILL_ENTER -> {
                    view.findViewById(R.id.MediaPlayerEmbeddedPiP).setVisibility(View.VISIBLE);

                    PictureInPictureParams.Builder pictureInPictureParams = new PictureInPictureParams.Builder().setSourceRectHint(_mediaPlayerState.sourceRectHint.get()).setAspectRatio(new Rational(_android.width, _android.height));

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        pictureInPictureParams.setAutoEnterEnabled(_android.automaticallyEnterPiP);
                        pictureInPictureParams.setSeamlessResizeEnabled(true);
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        pictureInPictureParams.setTitle(_extra.title);
                        pictureInPictureParams.setSubtitle(_extra.subtitle);
                        pictureInPictureParams.setExpandedAspectRatio(new Rational(_android.width, _android.height));
                    }
                    requireActivity().enterPictureInPictureMode(pictureInPictureParams.build());
                    _mediaPlayerState.pipState.set(UI_STATE.ACTIVE);
                }
                case WILL_EXIT -> {
                    view.findViewById(R.id.MediaPlayerEmbeddedPiP).setVisibility(View.GONE);
                    _mediaPlayerState.pipState.set(UI_STATE.INACTIVE);
                }
            }
        });

        int defaultUiVisibility = requireActivity().getWindow().getDecorView().getSystemUiVisibility();
        int fullscreenUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        ActionBar actionBar = getSupportActionBar();
        _mediaPlayerState.fullscreenState.observe(state -> {
            switch (state) {
                case ACTIVE -> {
                    MediaPlayerNotificationCenter.post(MediaPlayerNotification.create(_playerId, MediaPlayerNotificationCenter.NOTIFICATION_TYPE.MEDIA_PLAYER_FULLSCREEN).addData("isInFullScreen", true).build());
                }
                case INACTIVE -> {
                    MediaPlayerNotificationCenter.post(MediaPlayerNotification.create(_playerId, MediaPlayerNotificationCenter.NOTIFICATION_TYPE.MEDIA_PLAYER_FULLSCREEN).addData("isInFullScreen", false).build());
                }
                case WILL_ENTER -> {
                    view.findViewById(R.id.MediaPlayerFullscreenContainer).getGlobalVisibleRect(_sourceRectHint);
                    _mediaPlayerState.sourceRectHint.set(_sourceRectHint);

                    getChildFragmentManager().beginTransaction().detach(_playerControllerEmbeddedView).add(R.id.MediaPlayerFullscreenContainer, _playerControllerFullscreenView, "fullscreen").commit();
                    view.findViewById(R.id.MediaPlayerFullscreenContainer).setVisibility(View.VISIBLE);

                    requireActivity().getWindow().getDecorView().setSystemUiVisibility(fullscreenUiVisibility);

                    if (actionBar != null) {
                        actionBar.hide();
                    }

                    _mediaPlayerState.fullscreenState.set(UI_STATE.ACTIVE);
                }
                case WILL_EXIT -> {
                    view.findViewById(R.id.MediaPlayerEmbeddedContainer).getGlobalVisibleRect(_sourceRectHint);
                    _mediaPlayerState.sourceRectHint.set(_sourceRectHint);

                    getChildFragmentManager().beginTransaction().remove(_playerControllerFullscreenView).attach(_playerControllerEmbeddedView).commit();
                    view.findViewById(R.id.MediaPlayerFullscreenContainer).setVisibility(View.GONE);

                    requireActivity().getWindow().getDecorView().setSystemUiVisibility(defaultUiVisibility);

                    if (actionBar != null) {
                        actionBar.show();
                    }

                    _mediaPlayerState.fullscreenState.set(UI_STATE.INACTIVE);
                }
            }
        });
        _mediaPlayerState.landscapeState.observe(state -> {
            if (_android.fullscreenOnLandscape && _mediaPlayerState.pipState.get() == UI_STATE.INACTIVE) {
                if (state == UI_STATE.ACTIVE && _mediaPlayerState.fullscreenState.get() == UI_STATE.INACTIVE) {
                    _mediaPlayerState.fullscreenState.set(UI_STATE.WILL_ENTER);
                }
                if (state == UI_STATE.INACTIVE && _mediaPlayerState.fullscreenState.get() == UI_STATE.ACTIVE) {
                    _mediaPlayerState.fullscreenState.set(UI_STATE.WILL_EXIT);
                }
            }
        });

        getChildFragmentManager().beginTransaction().add(R.id.MediaPlayerEmbeddedPlayer, _playerControllerEmbeddedView, "embedded").commit();

        view.findViewById(R.id.MediaPlayerEmbeddedContainer).getGlobalVisibleRect(_sourceRectHint);
        _mediaPlayerState.sourceRectHint.set(_sourceRectHint);

        if (_android.openInFullscreen) {
            _mediaPlayerState.fullscreenState.set(UI_STATE.WILL_ENTER);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View containerView = inflater.inflate(R.layout.media_player_container, container, false);

        _playerController = new MediaPlayerController(requireContext(), _playerId, _android, _extra);
        _playerController.addMediaItem(new MediaItem(Uri.parse(_url), _extra));

        _mediaPlayerState.playerController.set(_playerController);

        _playerControllerEmbeddedView = new MediaPlayerControllerView(_playerId, MEDIA_PLAYER_VIEW_TYPE.EMBEDDED);
        _playerControllerFullscreenView = new MediaPlayerControllerView(_playerId, MEDIA_PLAYER_VIEW_TYPE.FULLSCREEN);

        View embeddedView = containerView.findViewById(R.id.MediaPlayerEmbeddedContainer);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(_android.width, _android.height);
        params.topMargin = _android.top;
        params.setMarginStart(_android.start);
        embeddedView.setLayoutParams(params);

        return containerView;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        boolean isLandscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE;
        _mediaPlayerState.landscapeState.set(isLandscape ? UI_STATE.ACTIVE : UI_STATE.INACTIVE);
    }

    @Nullable
    private ActionBar getSupportActionBar() {
        ActionBar actionBar = null;
        if (requireActivity() instanceof AppCompatActivity activity) {
            actionBar = activity.getSupportActionBar();
        }
        return actionBar;
    }
    
    @Override
    public void onPause() {
        if (_android.enableBackgroundPlay) {
            MediaPlayerNotificationCenter.post(MediaPlayerNotification.create(_playerId, MediaPlayerNotificationCenter.NOTIFICATION_TYPE.MEDIA_PLAYER_BACKGROUND_PLAYING).addData("isPlayingInBackground", true).build());
        } else {
            if (_mediaPlayerState.pipState.get() != UI_STATE.ACTIVE) {
                _playerController.pause();
            }
        }

        super.onPause();
    }

    @Override
    public void onResume() {
        if (_android.enableBackgroundPlay) {
            MediaPlayerNotificationCenter.post(MediaPlayerNotification.create(_playerId, MediaPlayerNotificationCenter.NOTIFICATION_TYPE.MEDIA_PLAYER_BACKGROUND_PLAYING).addData("isPlayingInBackground", false).build());
        }

        super.onResume();
    }
}
