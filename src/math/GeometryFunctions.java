package math;

import static java.lang.Math.PI;

public class GeometryFunctions {
    private static final double epsilon = 1e-9;

    public static Point2D add2Points(Point2D pointA, Point2D pointB) {
        return pointA.add(pointB);
    }

    public static Point2D midpoint(Point2D pointA, Point2D pointB) {
        return new Point2D((pointA.x + pointB.x)/2, (pointA.y + pointB.y)/2);
    }

    public static Point2D shiftPoint(Point2D point, double dx, double dy) {
        return point.shift(dx, dy);
    }

    public static double distance(Point2D p1, Point2D p2) {
        return p1.distance(p2);
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

    public static Point2D intersection(Line2D lineA, Line2D lineB) {
        double intersectionX = 1 / (lineA.a * lineB.b - lineA.b * lineB.a) * (lineA.b * lineB.c - lineA.c * lineB.b);
        double intersectionY = 1 / (lineA.a * lineB.b - lineA.b * lineB.a) * (lineA.c * lineB.a - lineA.a * lineB.c);
        return new Point2D(intersectionX, intersectionY);
    }

    public static boolean arePointsInALine(Point2D pointA, Point2D pointB, Point2D pointC) {
        if (pointA.equals(pointB) || pointB.equals(pointC) || pointC.equals(pointA))
            return true;

        return isParallel(new Line2D(pointA, pointB), new Line2D(pointA, pointC));
    }

    public static boolean isParallel(Line2D lineA, Line2D lineB) {
        return Math.abs(lineA.a*lineB.b - lineA.b*lineB.a) < epsilon;
    }
}
