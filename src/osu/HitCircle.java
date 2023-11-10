package osu;

import java.awt.geom.Point2D;

public class HitCircle extends HitObject{
    public HitCircle(String circleString) {
        super(circleString);
    }

    @Override
    public Point2D.Double endPosition() {
        return position();
    }

    @Override
    public double endTime() {
        return time;
    }
}
