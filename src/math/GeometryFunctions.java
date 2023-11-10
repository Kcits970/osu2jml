package math;

import java.awt.geom.Point2D;

import static java.lang.Math.PI;

public class GeometryFunctions {
    private static final double epsilon = 1e-9;

    public static Point2D.Double add2Points(Point2D.Double pointA, Point2D.Double pointB) {
        return new Point2D.Double(pointA.x + pointB.x, pointA.y + pointB.y);
    }

    public static Point2D.Double midpoint(Point2D.Double pointA, Point2D.Double pointB) {
        return new Point2D.Double((pointA.x + pointB.x)/2, (pointA.y + pointB.y)/2);
    }

    public static Point2D.Double shiftPoint(Point2D.Double point, double dx, double dy) {
        return new Point2D.Double(point.x + dx, point.y + dy);
    }

    public static double distance(Point2D.Double p1, Point2D.Double p2) {
        return Math.hypot(p1.x - p2.x, p1.y - p2.y);
    }

    public static double clockwiseAngle(Vector2D vectorA, Vector2D vectorB) {
        if (vectorA.direction() > vectorB.direction())
            return vectorA.direction() - vectorB.direction();
        else
            return 2*PI + vectorA.direction() - vectorB.direction();
    }

    public static double counterclockwiseAngle(Vector2D vectorA, Vector2D vectorB) {
        return 2*PI - clockwiseAngle(vectorA, vectorB);
    }

    public static Point2D.Double intersection(Line2D lineA, Line2D lineB) {
        double intersectionX = 1 / (lineA.a * lineB.b - lineA.b * lineB.a) * (lineA.b * lineB.c - lineA.c * lineB.b);
        double intersectionY = 1 / (lineA.a * lineB.b - lineA.b * lineB.a) * (lineA.c * lineB.a - lineA.a * lineB.c);
        return new Point2D.Double(intersectionX, intersectionY);
    }

    public static boolean arePointsInALine(Point2D.Double pointA, Point2D.Double pointB, Point2D.Double pointC) {
        if (pointA.equals(pointB) || pointB.equals(pointC) || pointC.equals(pointA))
            return true;

        return isParallel(new Line2D(pointA, pointB), new Line2D(pointA, pointC));
    }

    public static boolean isParallel(Line2D lineA, Line2D lineB) {
        return Math.abs(lineA.a*lineB.b - lineA.b*lineB.a) < epsilon;
    }
}
