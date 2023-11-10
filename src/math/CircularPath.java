package math;

import java.awt.geom.Point2D;
import static java.lang.Math.PI;
import static math.GeometryFunctions.*;

public class CircularPath extends SliderPath {
    Point2D.Double center;
    double radius;
    double angle;

    public CircularPath(Point2D.Double pointA, Point2D.Double pointB, Point2D.Double pointC) {
        Line2D lineAB = new Line2D(pointA, pointB);
        Line2D lineBC = new Line2D(pointB, pointC);

        center = intersection(
                new Line2D(midpoint(pointA, pointB), new Vector2D(lineAB.a, lineAB.b)),
                new Line2D(midpoint(pointB, pointC), new Vector2D(lineBC.a, lineBC.b))
        );

        startPoint = pointA;
        endPoint = pointC;
        radius = center.distance(pointA);

        double clockwiseAngle =
                clockwiseAngleOf2Vectors(new Vector2D(center, pointA), new Vector2D(center, pointB)) +
                clockwiseAngleOf2Vectors(new Vector2D(center, pointB), new Vector2D(center, pointC));
        double counterclockwiseAngle =
                counterclockwiseAngleOf2Vectors(new Vector2D(center, pointA), new Vector2D(center, pointB)) +
                counterclockwiseAngleOf2Vectors(new Vector2D(center, pointB), new Vector2D(center, pointC));

        angle = (clockwiseAngle < 2*PI) ? -clockwiseAngle : counterclockwiseAngle;
        length = radius * Math.abs(angle);
    }

    @Override
    public Point2D.Double getPointAtLength(double l) {
        double theta = angle * l/length;
        double startAngle = new Vector2D(center, startPoint).direction();

        return new Point2D.Double(
                center.x + radius * Math.cos(startAngle + theta),
                center.y + radius * Math.sin(startAngle + theta));
    }
}
