package osu.path;

import java.awt.geom.Point2D;

public interface SliderPath {
    double length();
    Point2D.Double pointAtLength(double l);
    SliderPath translate(double dx, double dy);
    SliderPath flip();
}
