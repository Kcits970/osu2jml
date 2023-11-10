package osu.path;

import math.*;
import osu.BeatmapConstants;

public class LinearPath implements SliderPath {
    Point2D start;
    Point2D end;

    public LinearPath(Point2D start, Point2D end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public double length() {
        return start.distance(end);
    }

    @Override
    public Point2D pointAtLength(double l) {
        return GeometryFunctions.add2Points(start, new Vector2D(start, end).scale(l/length()).toPoint());
    }

    @Override
    public LinearPath translate(double dx, double dy) {
        return new LinearPath(
                GeometryFunctions.shiftPoint(start, dx, dy),
                GeometryFunctions.shiftPoint(end, dx, dy)
        );
    }

    @Override
    public LinearPath flip() {
        return new LinearPath(
                new Point2D(start.x, BeatmapConstants.SCREEN_HEIGHT - start.y),
                new Point2D(end.x, BeatmapConstants.SCREEN_HEIGHT - end.y)
        );
    }
}
