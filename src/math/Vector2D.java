package math;

import java.awt.geom.Point2D;
import static java.lang.Math.*;

public class Vector2D {
    public final double x;
    public final double y;

    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2D(Point2D.Double tail, Point2D.Double head) {
        x = head.getX() - tail.getX();
        y = head.getY() - tail.getY();
    }

    public double magnitude() {
        return hypot(x, y);
    }

    public double direction() {
        return atan2(y, x);
    }

    public Vector2D scale(double scalar) {
        return new Vector2D(x * scalar, y * scalar);
    }

    public Point2D.Double toPoint() {
        return new Point2D.Double(x,y);
    }
}
