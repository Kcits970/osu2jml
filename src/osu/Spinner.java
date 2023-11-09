package osu;

import java.awt.geom.Point2D;

public class Spinner extends HitObject {
    static final int END_TIME_INDEX = 5;
    public static final int DEFAULT_X = BeatmapConstants.SCREEN_WIDTH/2;
    public static final int DEFAULT_Y = BeatmapConstants.SCREEN_HEIGHT/2;

    public Spinner(String spinnerString) {
        super(spinnerString);
        x = DEFAULT_X;
        y = DEFAULT_Y;
        endTime = Integer.parseInt(spinnerString.split(",")[END_TIME_INDEX]);
        endPosition = position;
    }

    @Override
    public void shift(Point2D.Double unit) {}
}
