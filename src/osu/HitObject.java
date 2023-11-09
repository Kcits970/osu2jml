package osu;

import java.awt.geom.Point2D;

public abstract class HitObject {
    static final int X_INDEX = 0;
    static final int Y_INDEX = 1;
    static final int TIME_INDEX = 2;

    public int x, y, stackLayer;
    public double time, endTime;
    public Point2D.Double position, endPosition;

    public HitObject(String hitObjectString) {
        String[] circleParameters = hitObjectString.split(",");
        x = Integer.parseInt(circleParameters[X_INDEX]);
        y = Integer.parseInt(circleParameters[Y_INDEX]);
        time = Integer.parseInt(circleParameters[TIME_INDEX]);
        endTime = time;
        position = new Point2D.Double(x, y);
        endPosition = position;
    }

    public void resetStackProperties() {
        stackLayer = 0;
    }

    public void scaleTime(double percentage) {
        time *= percentage;
        endTime *= percentage;
    }

    public void shift(Point2D.Double unit) {
        Point2D.Double shiftingVector = new Point2D.Double(unit.x * -stackLayer, unit.y * -stackLayer);

        x += shiftingVector.x;
        y += shiftingVector.y;
        position = new Point2D.Double(x, y);
        endPosition = position;
    }

    public void flip() {
        y = osuConstants.SCREEN_HEIGHT - y;
        position = new Point2D.Double(x, y);
        endPosition = position;
    }
}