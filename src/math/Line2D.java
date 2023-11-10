package math;

import java.awt.geom.Point2D;

public class Line2D {
    //represents the line ax + by + c = 0

    public final double a, b, c;

    public Line2D(double a, double b, double c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public Line2D(Point2D.Double point, Vector2D vector) {
        this(vector.y, -vector.x, vector.x*point.y - vector.y*point.x);
    }

    public Line2D(Point2D.Double pointA, Point2D.Double pointB) {
        this(pointA, new Vector2D(pointA, pointB));
    }
}
