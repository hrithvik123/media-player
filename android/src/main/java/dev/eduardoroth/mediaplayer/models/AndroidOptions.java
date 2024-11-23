package dev.eduardoroth.mediaplayer.models;

public class AndroidOptions {
    public boolean enableChromecast;
    public boolean enablePiP;
    public boolean enableBackgroundPlay;
    public boolean openInFullscreen;
    public boolean automaticallyEnterPiP;
    public boolean fullscreenOnLandscape;
    public double top;
    public double left;
    public double height;
    public double width;

    public AndroidOptions(Boolean enableChromecast, Boolean enablePiP, Boolean enableBackgroundPlay, Boolean openInFullscreen, Boolean automaticallyEnterPiP, Boolean fullscreenOnLandscape, Double top, Double left, Double height, Double width) {
        this.enableChromecast = enableChromecast;
        this.enablePiP = enablePiP;
        this.enableBackgroundPlay = enableBackgroundPlay;
        this.openInFullscreen = openInFullscreen;
        this.automaticallyEnterPiP = automaticallyEnterPiP;
        this.fullscreenOnLandscape = fullscreenOnLandscape;
        this.top = top;
        this.left = left;
        this.height = height;
        this.width = width;
    }
}
