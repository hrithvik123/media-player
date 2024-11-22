package dev.eduardoroth.mediaplayer.models;

public class SubtitleSettings {
    public String language;
    public String foregroundColor;
    public String backgroundColor;
    public Double fontSize;

    public SubtitleSettings(String language, String foregroundColor, String backgroundColor, Double fontSize) {
        this.language = language;
        this.foregroundColor = foregroundColor;
        this.backgroundColor = backgroundColor;
        this.fontSize = fontSize != null ? fontSize : Double.valueOf("12");
    }
}
