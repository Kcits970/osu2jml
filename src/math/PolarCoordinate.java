package math;

import java.awt.geom.Point2D;

public class PolarCoordinate {
    public final double r;
    public final double theta; //theta is in terms of radians.

    public PolarCoordinate(double r, double theta) {
        this.r = r;
        this.theta = theta;
    }

    public Point2D.Double toCartesian() {
        return new Point2D.Double(r * Math.cos(theta), r * Math.sin(theta));
    }
}
