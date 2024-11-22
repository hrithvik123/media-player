package dev.eduardoroth.mediaplayer.utilities;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.net.Uri;
import android.util.TypedValue;

import androidx.media3.common.C;
import androidx.media3.common.Format;
import androidx.media3.common.MediaItem.SubtitleConfiguration;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.Player;
import androidx.media3.datasource.DataSource;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.MergingMediaSource;
import androidx.media3.exoplayer.source.SingleSampleMediaSource;
import androidx.media3.extractor.text.SubtitleParser;
import androidx.media3.ui.CaptionStyleCompat;
import androidx.media3.ui.PlayerView;
import androidx.media3.ui.SubtitleView;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import dev.eduardoroth.mediaplayer.models.SubtitleOptions;

@SuppressLint("UnsafeOptInUsageError")
public class SubtitlesHelpers {

    public String language;

    private final PlayerView playerView;
    private final SubtitleOptions subtitleOptions;
    private Uri subtitleUri;

    public SubtitlesHelpers(SubtitleOptions options, PlayerView playerView) {
        this.subtitleOptions = options;
        this.playerView = playerView;
        try {
            this.subtitleUri = Uri.parse(this.subtitleOptions.url.toString());
        } catch (Exception ignored) {
        }
    }

    public boolean hasSubtitles(){
        return this.subtitleUri != null;
    }

    public SubtitleConfiguration getSubtitlesFactory() {
        String mimeType = getMimeType(subtitleUri);
        String languageLabel = Locale.forLanguageTag(language).getDisplayLanguage();
        return new SubtitleConfiguration.Builder(subtitleUri)
                .setMimeType(mimeType)
                .setUri(subtitleUri)
                .setId(subtitleOptions.url.toString())
                .setLabel(languageLabel)
                .setRoleFlags(C.ROLE_FLAG_CAPTION)
                .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                .setLanguage(language)
                .build();
    }

    public void setSubtitle(boolean transparent) {
        int foreground;
        int background;
        if (!transparent) {
            foreground = Color.WHITE;
            background = Color.BLACK;
            if (subtitleOptions.settings.foregroundColor.length() > 4 && subtitleOptions.settings.foregroundColor.startsWith("rgba")) {
                foreground = getColorFromRGBA(subtitleOptions.settings.foregroundColor);
            }
            if (subtitleOptions.settings.backgroundColor.length() > 4 && subtitleOptions.settings.backgroundColor.startsWith("rgba")) {
                background = getColorFromRGBA(subtitleOptions.settings.backgroundColor);
            }
        } else {
            foreground = Color.TRANSPARENT;
            background = Color.TRANSPARENT;
        }
        SubtitleView subView = playerView.getSubtitleView();
        if (subView != null) {
            subView.setStyle(
                    new CaptionStyleCompat(foreground, background, Color.TRANSPARENT, CaptionStyleCompat.EDGE_TYPE_NONE, Color.WHITE, null)
            );
            subView.setFixedTextSize(TypedValue.COMPLEX_UNIT_DIP, subtitleOptions.settings.fontSize.floatValue());
            playerView.setShowSubtitleButton(true);
        }
    }

    private int getColorFromRGBA(String rgbaColor) {
        int ret = 0;
        String color = rgbaColor.substring(rgbaColor.indexOf("(") + 1, rgbaColor.indexOf(")"));
        List<String> colors = Arrays.asList(color.split(","));
        if (colors.size() == 4) {
            ret =
                    (Math.round(Float.parseFloat(colors.get(3).trim()) * 255) & 0xff) << 24 |
                            (Integer.parseInt(colors.get(0).trim()) & 0xff) << 16 |
                            (Integer.parseInt(colors.get(1).trim()) & 0xff) << 8 |
                            (Integer.parseInt(colors.get(2).trim()) & 0xff);
        }
        return ret;
    }

    private String getMimeType(Uri subtitlesUrl) {
        String lastSegment = subtitlesUrl.getLastPathSegment();
        if (lastSegment != null) {
            String extension = lastSegment.substring(lastSegment.lastIndexOf(".") + 1);
            return switch (extension) {
                case "vtt" -> MimeTypes.TEXT_VTT;
                case "srt" -> MimeTypes.APPLICATION_SUBRIP;
                case "ssa", "ass" -> MimeTypes.TEXT_SSA;
                case "ttml", "dfxp", "xml" -> MimeTypes.APPLICATION_TTML;
                default -> "";
            };
        }
        return "";
    }
}
