package osu;

import math.Point2D;

public abstract class HitObject {
    //Parsing hit objects follow information from the osu! file format wiki:
    //https://osu.ppy.sh/wiki/en/Client/File_formats/osu_%28file_format%29#hit-objects
    protected static final int X_INDEX = 0;
    protected static final int Y_INDEX = 1;
    protected static final int TIME_INDEX = 2;

    public double x, y, time;

    public HitObject(double x, double y, double time) {
        this.x = x;
        this.y = y;
        this.time = time;
    }

    public HitObject(String hitObjectString) {
        String[] circleParameters = hitObjectString.split(",");
        x = Integer.parseInt(circleParameters[X_INDEX]);
        y = Integer.parseInt(circleParameters[Y_INDEX]);
        time = Integer.parseInt(circleParameters[TIME_INDEX]);
    }

    public Point2D position() {
        return new Point2D(x, y);
    }

    public abstract Point2D endPosition();

    public abstract double endTime();

    public void adjustSpeed(double percentage) {
        time *= 1/percentage;
    }

    public void translate(double dx, double dy) {
        x += dx;
        y += dy;
    }

    public void flip() {
        y = BeatmapConstants.SCREEN_HEIGHT - y;
    }
}