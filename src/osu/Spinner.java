package osu;

import math.Point2D;

public class Spinner extends HitObject {
    private static final int END_TIME_INDEX = 5;
    public static final int DEFAULT_X = BeatmapFunctions.SCREEN_WIDTH/2;
    public static final int DEFAULT_Y = BeatmapFunctions.SCREEN_HEIGHT/2;
    public double endTime;

    public Spinner(String spinnerString) {
        super(spinnerString);
        x = DEFAULT_X;
        y = DEFAULT_Y;
        endTime = Integer.parseInt(spinnerString.split(",")[END_TIME_INDEX]);
    }

    @Override
    public Point2D endPosition() {
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
