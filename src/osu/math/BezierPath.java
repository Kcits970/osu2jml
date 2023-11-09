package osu.math;

import java.awt.geom.Point2D;
import java.util.List;
import static osu.math.AlgebraFunctions.*;

public class BezierPath extends SliderPath {
    static final double STANDARD_SUBINTERVAL_LENGTH = 0.01;

    List<Point2D.Double> controlPoints;

    public BezierPath(List<Point2D.Double> controlPoints) {
        this.controlPoints = controlPoints;
        this.startPoint = controlPoints.get(0);
        this.endPoint = controlPoints.get(controlPoints.size() - 1);
        this.length = approximateLengthInRange(0, 1);
    }

    @Override
    public Point2D.Double getPointAtLength(double l) {
        if (l == 0)
            return startPoint;

        if (l == length)
            return endPoint;

        for (double t = 0; ; t += STANDARD_SUBINTERVAL_LENGTH) {
            double lengthToCompare = approximateLengthInRange(0, t);

            if (lengthToCompare == l)
                return getPointAt(t);
            if (lengthToCompare > l)
                return getPointAt(algebraicMean(t, t - STANDARD_SUBINTERVAL_LENGTH));
        }
    }

    private Point2D.Double getPointAt(double t) {
        Point2D.Double point = new Point2D.Double();
        int lastControlPointIndex = controlPoints.size() - 1;

        for (int k = 0; k < controlPoints.size(); k++) {
            double bernsteinValue = bernstein(lastControlPointIndex,k,t);
            point.x += bernsteinValue * controlPoints.get(k).x;
            point.y += bernsteinValue * controlPoints.get(k).y;
        }

        return point;
    }

    private Point2D.Double getDifferentiatedPointAt(double t) {
        Point2D.Double differentiatedPoint = new Point2D.Double();
        int lastControlPointIndex = controlPoints.size() - 1;

        for (int k = 0; k < controlPoints.size(); k++) {
            double bernsteinPrimeValue = bernsteinPrime(lastControlPointIndex,k,t);
            differentiatedPoint.x += bernsteinPrimeValue * controlPoints.get(k).x;
            differentiatedPoint.y += bernsteinPrimeValue * controlPoints.get(k).y;
        }

        return differentiatedPoint;
    }

    private double curveLengthIntegrand(double t) {
        Point2D.Double differentiatedPoint = getDifferentiatedPointAt(t);
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
}
