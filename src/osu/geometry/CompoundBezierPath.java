package osu.geometry;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class CompoundBezierPath extends SliderPath {
    List<SliderPath> sliderPaths;

    public CompoundBezierPath(List<Point2D.Double> controlPoints) {
        startPoint = controlPoints.get(0);
        endPoint = controlPoints.get(controlPoints.size() - 1);
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

        for (SliderPath path : sliderPaths)
            length += path.length;
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
    public Point2D.Double getPointAtLength(double l) {
        double lengthAccumulation = 0;

        for (SliderPath currentPath : sliderPaths) {
            lengthAccumulation += currentPath.length;

            if (l < lengthAccumulation)
                return currentPath.getPointAtLength(l - (lengthAccumulation - currentPath.length));
        }

        SliderPath lastPath = sliderPaths.get(sliderPaths.size() - 1);
        return lastPath.getPointAtLength(lastPath.length + l - length);
    }
}
