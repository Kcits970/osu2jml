package osu.math;

import java.awt.geom.Point2D;

public class LinearPath extends SliderPath {
    public LinearPath(Point2D.Double startPoint, Point2D.Double endPoint) {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.length = startPoint.distance(endPoint);
    }

    @Override
    public Point2D.Double getPointAtLength(double l) {
        Vector2D pathVector = new Vector2D(startPoint, endPoint);
        pathVector.scale(l/length);

        return GeometryFunctions.add2Points(startPoint, pathVector.pointRepresentation());
    }
}
