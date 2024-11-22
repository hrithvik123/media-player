package dev.eduardoroth.mediaplayer.models;

import android.net.Uri;

public class SubtitleOptions {
    public Uri url;
    public SubtitleSettings settings;

    public SubtitleOptions(Uri url, String language, String foregroundColor, String backgroundColor, Double fontSize){
        this.url = url;
        this.settings = new SubtitleSettings(language, foregroundColor, backgroundColor, fontSize);
    }

    public SubtitleOptions(Uri url, SubtitleSettings settings){
        this.url = url;
        this.settings = settings;
    }
}
