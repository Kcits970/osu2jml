package osu.path;

import math.*;
import osu.BeatmapConstants;

import static java.lang.Math.PI;
import static math.GeometryFunctions.*;

public class CircularPath implements SliderPath {
    Point2D pointA, pointB, pointC, center;
    double radius;
    double angle;

    public CircularPath(Point2D pointA, Point2D pointB, Point2D pointC) {
        this.pointA = pointA;
        this.pointB = pointB;
        this.pointC = pointC;

        Line2D lineAB = new Line2D(pointA, pointB);
        Line2D lineBC = new Line2D(pointB, pointC);

        center = intersection(
                new Line2D(midpoint(pointA, pointB), new Vector2D(lineAB.a, lineAB.b)),
                new Line2D(midpoint(pointB, pointC), new Vector2D(lineBC.a, lineBC.b))
        );

        radius = center.distance(pointA);

        double clockwiseAngle =
                clockwiseAngle(new Vector2D(center, pointA), new Vector2D(center, pointB)) +
                clockwiseAngle(new Vector2D(center, pointB), new Vector2D(center, pointC));
        double counterclockwiseAngle =
                counterclockwiseAngle(new Vector2D(center, pointA), new Vector2D(center, pointB)) +
                counterclockwiseAngle(new Vector2D(center, pointB), new Vector2D(center, pointC));

        angle = (clockwiseAngle < 2*PI) ? -clockwiseAngle : counterclockwiseAngle;
    }

    @Override
    public double length() {
        return radius * Math.abs(angle);
    }

    @Override
    public Point2D pointAtLength(double l) {
        double theta = angle * l/length();
        double startAngle = new Vector2D(center, pointA).direction();

        return new Point2D(
                center.x + radius * Math.cos(startAngle + theta),
                center.y + radius * Math.sin(startAngle + theta));
    }

    @Override
    public CircularPath translate(double dx, double dy) {
        return new CircularPath(
                GeometryFunctions.shiftPoint(pointA, dx, dy),
                GeometryFunctions.shiftPoint(pointB, dx, dy),
                GeometryFunctions.shiftPoint(pointC, dx, dy)
        );
    }

    @Override
    public CircularPath flip() {
        return new CircularPath(
                new Point2D(pointA.x, BeatmapConstants.SCREEN_HEIGHT - pointA.y),
                new Point2D(pointB.x, BeatmapConstants.SCREEN_HEIGHT - pointB.y),
                new Point2D(pointC.x, BeatmapConstants.SCREEN_HEIGHT - pointC.y)
        );
    }
}
