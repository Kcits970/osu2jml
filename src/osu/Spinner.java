package osu;

import java.awt.geom.Point2D;

public class Spinner extends HitObject {
    private static final int END_TIME_INDEX = 5;
    private static final int DEFAULT_X = BeatmapConstants.SCREEN_WIDTH/2;
    private static final int DEFAULT_Y = BeatmapConstants.SCREEN_HEIGHT/2;
    public double endTime;

    public Spinner(String spinnerString) {
        super(spinnerString);
        x = DEFAULT_X;
        y = DEFAULT_Y;
        endTime = Integer.parseInt(spinnerString.split(",")[END_TIME_INDEX]);
    }

    @Override
    public Point2D.Double endPosition() {
        return position();
    }

    @Override
    public double endTime() {
        return endTime;
    }

    @Override
    public void adjustSpeed(double percentage) {
        time *= 1/percentage;
        endTime *= 1/percentage;
    }

    @Override
    public void translate(double dx, double dy) {}

    @Override
    public void flip() {}
}
