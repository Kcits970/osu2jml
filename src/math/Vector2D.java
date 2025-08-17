package math;

import static java.lang.Math.*;

public class Vector2D {
    public final double x;
    public final double y;

    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2D(Point2D tail, Point2D head) {
        x = head.x - tail.x;
        y = head.y - tail.y;
    }

    public double direction() {
        return atan2(y, x);
    }

    public Vector2D scale(double scalar) {
        return new Vector2D(x * scalar, y * scalar);
    }

    public Point2D toPoint() {
        return new Point2D(x,y);
    }
}
