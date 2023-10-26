package osu.geometry;

import java.awt.geom.Point2D;

public abstract class SliderPath {
    public Point2D.Double startPoint;
    public Point2D.Double endPoint;
    public double length;

    public abstract Point2D.Double getPointAtLength(double l);
}
