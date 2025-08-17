package math;

public class Point2D {
    public final double x;
    public final double y;

    public Point2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Point2D anotherPoint))
            return false;

        return x == anotherPoint.x && y == anotherPoint.y;
    }

    public Point2D add(Point2D anotherPoint) {
        return new Point2D(x + anotherPoint.x, y + anotherPoint.y);
    }

    public Point2D shift(double dx, double dy) {
        return new Point2D(x + dx, y + dy);
    }

    public double distance(Point2D anotherPoint) {
        return Math.hypot(anotherPoint.x - x, anotherPoint.y - y);
    }
}
