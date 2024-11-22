package dev.eduardoroth.mediaplayer.models;
import android.net.Uri;
import com.getcapacitor.JSObject;

public class ExtraOptions {
    public String title;
    public String subtitle;
    public Uri poster;
    public String artist;
    public Double rate;
    public SubtitleOptions subtitles;
    public Boolean loopOnEnd;
    public Boolean showControls;
    public JSObject headers;

    public ExtraOptions(String title, String subtitle, Uri poster, String artist, Double rate, SubtitleOptions subtitles, Boolean loopOnEnd, Boolean showControls, JSObject headers) {
        this.title = title;
        this.subtitle = subtitle;
        this.poster = poster;
        this.artist = artist;
        this.rate = rate;
        this.subtitles = subtitles;
        this.loopOnEnd = loopOnEnd;
        this.showControls = showControls;
        this.headers = headers;
    }
}
