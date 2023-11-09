package math;

import java.awt.geom.Point2D;
import static java.lang.Math.*;

public class Vector2D {
    public Point2D.Double origin;
    public double x;
    public double y;

    public Vector2D(double x, double y) {
        origin = new Point2D.Double();
        this.x = x;
        this.y = y;
    }

    public Vector2D(Point2D.Double tail, Point2D.Double head) {
        origin = tail;
        x = head.getX() - tail.getX();
        y = head.getY() - tail.getY();
    }

    public double magnitude() {
        return hypot(x, y);
    }

    public double direction() {
        return atan2(y, x);
    }

    public void scale(double scalar) {
        x *= scalar;
        y *= scalar;
    }

    public Point2D.Double pointRepresentation() {
        return new Point2D.Double(x,y);
    }
}
