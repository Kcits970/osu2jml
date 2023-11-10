package osu.path;

import math.GeometryFunctions;
import osu.BeatmapConstants;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class CompoundBezierPath implements SliderPath {
    private List<Point2D.Double> controlPoints;
    List<SliderPath> sliderPaths;

    public CompoundBezierPath(List<Point2D.Double> controlPoints) {
        this.controlPoints = controlPoints;
        sliderPaths = new ArrayList<>();

        int lastDuplicateIndex = 0;
        for (int i = 1; i < controlPoints.size(); i++) {
            Point2D currentPoint = controlPoints.get(i);
            Point2D previousPoint = controlPoints.get(i - 1);

            if (currentPoint.equals(previousPoint)) {
                addPath(controlPoints.subList(lastDuplicateIndex, i));
                lastDuplicateIndex = i;
            }
        }

        addPath(controlPoints.subList(lastDuplicateIndex, controlPoints.size()));
    }

    private void addPath(List<Point2D.Double> controlPoints) {
        if (controlPoints.size() == 1);
        else if (controlPoints.size() == 2)
            sliderPaths.add(new LinearPath(controlPoints.get(0), controlPoints.get(1)));
        else
            sliderPaths.add(new BezierPath(controlPoints));
    }

    public static boolean isCompound(List<Point2D.Double> controlPoints) {
        for (int i = 1; i < controlPoints.size(); i++) {
            Point2D currentPoint = controlPoints.get(i);
            Point2D previousPoint = controlPoints.get(i-1);

            if (currentPoint.equals(previousPoint))
                return true;
        }

        return false;
    }

    @Override
    public double length() {
        return sliderPaths.stream()
                .mapToDouble(SliderPath::length)
                .sum();
    }

    @Override
    public Point2D.Double pointAtLength(double l) {
        double lengthAccumulation = 0;

        for (SliderPath currentPath : sliderPaths) {
            lengthAccumulation += currentPath.length();

            if (l < lengthAccumulation)
                return currentPath.pointAtLength(l - (lengthAccumulation - currentPath.length()));
        }

        SliderPath lastPath = sliderPaths.get(sliderPaths.size() - 1);
        return lastPath.pointAtLength(lastPath.length() + l - length());
    }

    @Override
    public CompoundBezierPath translate(double dx, double dy) {
        List<Point2D.Double> newControlPoints = new ArrayList<>(controlPoints);
        newControlPoints.replaceAll(point -> GeometryFunctions.shiftPoint(point, dx, dy));

        return new CompoundBezierPath(newControlPoints);
    }

    @Override
    public CompoundBezierPath flip() {
        List<Point2D.Double> newControlPoints = new ArrayList<>(controlPoints);
        newControlPoints.replaceAll(point -> new Point2D.Double(point.x, BeatmapConstants.SCREEN_HEIGHT - point.y));

        return new CompoundBezierPath(newControlPoints);
    }
}
