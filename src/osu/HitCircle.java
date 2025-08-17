package osu;

import math.Point2D;

public class HitCircle extends HitObject{
    public HitCircle(String circleString) {
        super(circleString);
    }

    @Override
    public Point2D endPosition() {
        return position();
    }

    @Override
    public double endTime() {
        return time;
    }
}
