package osu.path;

import math.GeometryFunctions;
import math.Vector2D;
import osu.BeatmapConstants;

import java.awt.geom.Point2D;

public class LinearPath implements SliderPath {
    Point2D.Double start;
    Point2D.Double end;

    public LinearPath(Point2D.Double start, Point2D.Double end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public double length() {
        return start.distance(end);
    }

    @Override
    public Point2D.Double pointAtLength(double l) {
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
                new Point2D.Double(start.x, BeatmapConstants.SCREEN_HEIGHT - start.y),
                new Point2D.Double(end.x, BeatmapConstants.SCREEN_HEIGHT - end.y)
        );
    }
}
