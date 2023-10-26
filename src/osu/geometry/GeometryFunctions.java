package osu.geometry;

import java.awt.geom.Point2D;

import static java.lang.Math.PI;

public class GeometryFunctions {
    private static final double epsilon = 1e-10;

    public static Line2D lineFrom2Points(Point2D.Double pointA, Point2D.Double pointB) {
        return lineFromPointAndVector(pointA, new Vector2D(pointA, pointB));
    }
    public static Line2D lineFromPointAndVector(Point2D.Double point, Vector2D vector) {
        return new Line2D(vector.y, -vector.x, vector.x*point.y - vector.y*point.getX());
    }

    public static Point2D.Double intersection(Line2D lineA, Line2D lineB) {
        double intersectionX = 1 / (lineA.a * lineB.b - lineA.b * lineB.a) * (lineA.b * lineB.c - lineA.c * lineB.b);
        double intersectionY = 1 / (lineA.a * lineB.b - lineA.b * lineB.a) * (lineA.c * lineB.a - lineA.a * lineB.c);
        return new Point2D.Double(intersectionX, intersectionY);
    }

    public static Point2D.Double add2Points(Point2D.Double pointA, Point2D.Double pointB) {
        return new Point2D.Double(pointA.x + pointB.x, pointA.y + pointB.y);
    }

    public static Point2D.Double midpoint(Point2D.Double pointA, Point2D.Double pointB) {
        return new Point2D.Double((pointA.x + pointB.x)/2, (pointA.y + pointB.y)/2);
    }

    public static double clockwiseAngleOf2Vectors(Vector2D vectorA, Vector2D vectorB) {
        if (vectorA.direction() > vectorB.direction())
            return vectorA.direction() - vectorB.direction();
        else
            return 2*PI + vectorA.direction() - vectorB.direction();
    }

    public static double counterclockwiseAngleOf2Vectors(Vector2D vectorA, Vector2D vectorB) {
        return 2*PI - clockwiseAngleOf2Vectors(vectorA, vectorB);
    }

    public static double distance(Point2D.Double p1, Point2D.Double p2) {
        return Math.hypot(p1.x - p2.x, p1.y - p2.y);
    }

    public static boolean isPointAtOrigin(Point2D.Double p) {
        return p.x == 0 && p.y == 0;
    }

    public static boolean isVectorParallelToXAxis(Point2D.Double v) {
        if (isPointAtOrigin(v))
            return false;

        return v.y == 0;
    }

    public static boolean isVectorParallelToYAxis(Point2D.Double v) {
        if (isPointAtOrigin(v))
            return false;

        return v.x == 0;
    }

    public static boolean are2VectorsParallel(Point2D.Double v1, Point2D.Double v2) {
        if (isPointAtOrigin(v1) || isPointAtOrigin(v2))
            return false;

        if (isVectorParallelToXAxis(v1))
            return isVectorParallelToYAxis(v2);
        else if (isVectorParallelToYAxis(v1))
            return isVectorParallelToYAxis(v2);
        else
            return Math.abs(v2.x/v1.x - v2.y/v1.y) < epsilon;
    }

    public static boolean are3PointsInALine(Point2D.Double p1, Point2D.Double p2, Point2D.Double p3) {
        return are2VectorsParallel(new Vector2D(p1, p2).pointRepresentation(), new Vector2D(p1, p3).pointRepresentation());
    }
}
