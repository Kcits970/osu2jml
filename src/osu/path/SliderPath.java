package osu.path;

import math.Point2D;

public interface SliderPath {
    double length();
    Point2D pointAtLength(double l);
    SliderPath translate(double dx, double dy);
    SliderPath flip();
}
