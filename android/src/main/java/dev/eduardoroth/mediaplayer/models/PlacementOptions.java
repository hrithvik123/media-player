package dev.eduardoroth.mediaplayer.models;

import java.io.Serializable;

public class PlacementOptions implements Serializable {
    public String videoOrientation;
    public String horizontalAlignment;
    public String verticalAlignment;

    public int height;
    public int width;

    public int horizontalMargin;
    public int verticalMargin;

    public PlacementOptions(
            int height, int width, String videoOrientation, String horizontalAlignment, String verticalAlignment, int horizontalMargin, int verticalMargin
    ) {
        this.height = height;
        this.width = width;
        this.videoOrientation = videoOrientation;
        this.horizontalAlignment = horizontalAlignment;
        this.verticalAlignment = verticalAlignment;
        this.horizontalMargin = horizontalMargin;
        this.verticalMargin = verticalMargin;
    }
}
