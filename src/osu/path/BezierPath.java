package osu.path;

import math.*;
import osu.BeatmapFunctions;

import java.util.ArrayList;
import java.util.List;
import static math.AlgebraFunctions.*;

public class BezierPath implements SliderPath {
    static final double STANDARD_SUBINTERVAL_LENGTH = 0.01;

    List<Point2D> controlPoints;
    private double cacheLength = -1;

    public BezierPath(List<Point2D> controlPoints) {
        this.controlPoints = controlPoints;
    }

    @Override
    public double length() {
        if (cacheLength < 0)
            cacheLength = approximateLengthInRange(0, 1);

        return cacheLength;
    }

    @Override
    public Point2D pointAtLength(double l) {
        if (l == 0)
            return controlPoints.getFirst();

        for (double t = 0; ; t += STANDARD_SUBINTERVAL_LENGTH) {
            double lengthToCompare = approximateLengthInRange(0, t);

            if (lengthToCompare == l)
                return getPointAt(t);
            if (lengthToCompare > l)
                return getPointAt(algebraicMean(t, t - STANDARD_SUBINTERVAL_LENGTH));
        }
    }

    private Point2D getPointAt(double t) {
        double x = 0;
        double y = 0;
        int lastControlPointIndex = controlPoints.size() - 1;

        for (int k = 0; k < controlPoints.size(); k++) {
            double bernsteinValue = bernstein(lastControlPointIndex,k,t);
            x += bernsteinValue * controlPoints.get(k).x;
            y += bernsteinValue * controlPoints.get(k).y;
        }

        return new Point2D(x,y);
    }

    private Point2D getDifferentiatedPointAt(double t) {
        double x = 0;
        double y = 0;
        int lastControlPointIndex = controlPoints.size() - 1;

        for (int k = 0; k < controlPoints.size(); k++) {
            double bernsteinPrimeValue = bernsteinPrime(lastControlPointIndex,k,t);
            x += bernsteinPrimeValue * controlPoints.get(k).x;
            y += bernsteinPrimeValue * controlPoints.get(k).y;
        }

        return new Point2D(x,y);
    }

    private double curveLengthIntegrand(double t) {
        Point2D differentiatedPoint = getDifferentiatedPointAt(t);
        return Math.hypot(differentiatedPoint.x, differentiatedPoint.y);
    }

    private double approximateLengthInRange(double t1, double t2) {
        //approximates the length of the bezier curve B(t) from t=t1 to t=t2 using Simpson's Rule.

        double rangeLength = t2 - t1;
        int numOfSubintervals = (int) Math.ceil(rangeLength/STANDARD_SUBINTERVAL_LENGTH);
        if (numOfSubintervals % 2 == 1)
            numOfSubintervals++; //the number of subintervals must be even for Simpson's Rule to work.
        double adjustedSubintervalLength = rangeLength/numOfSubintervals;

        double accumulation = curveLengthIntegrand(t1) + curveLengthIntegrand(t2);

        for (int i = 1; i < numOfSubintervals; i++) {
            double subintervalValue = curveLengthIntegrand(t1 + adjustedSubintervalLength*i);
            accumulation += (i % 2 == 0) ? 2*subintervalValue : 4*subintervalValue;
        }

        return adjustedSubintervalLength/3 * accumulation;
    }

    @Override
    public BezierPath translate(double dx, double dy) {
        List<Point2D> newControlPoints = new ArrayList<>(controlPoints);
        newControlPoints.replaceAll(point -> GeometryFunctions.shiftPoint(point, dx, dy));

        return new BezierPath(newControlPoints);
    }

    @Override
    public BezierPath flip() {
        List<Point2D> newControlPoints = new ArrayList<>(controlPoints);
        newControlPoints.replaceAll(point -> new Point2D(point.x, BeatmapFunctions.SCREEN_HEIGHT - point.y));

        return new BezierPath(newControlPoints);
    }
}
