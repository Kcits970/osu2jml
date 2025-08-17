package math;

public class PolarCoordinate {
    public final double r;
    public final double theta; //theta is in terms of radians.

    public PolarCoordinate(double r, double theta) {
        this.r = r;
        this.theta = theta;
    }

    public Point2D toCartesian() {
        return new Point2D(r * Math.cos(theta), r * Math.sin(theta));
    }
}
