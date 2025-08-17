package jml;

import math.Point3D;

import java.util.List;
import java.util.ArrayList;

public class Event {
    public double x, y, z, t;
    public int juggler;
    public String hand;

    List<Manipulation> manipulations;

    public Event(double x, double y, double z, double t, int juggler, String hand) {
        if (!"left".equals(hand) && !"right".equals(hand))
            throw new RuntimeException(String.format("invalid hand specification: '%s'", hand));

        this.x = x;
        this.y = y;
        this.z = z;
        this.t = t;
        this.juggler = juggler;
        this.hand = hand;

        manipulations = new ArrayList<>();
    }

    public Event(Point3D point, double t, int juggler, String hand) {
        this(point.x, point.y, point.z, t, juggler, hand);
    }

    public static Event createDefaultEvent(String hand) {
        Event defaultEvent = new Event(0, 0, 0, 0, 1, hand);
        if (defaultEvent.hand.equals("left"))
            defaultEvent.x = -25;
        else
            defaultEvent.x = 25;

        return defaultEvent;
    }

    public Point3D getPosition() {
        return new Point3D(x, y, z);
    }

    public Event translate(double dx, double dy, double dz) {
        x += dx;
        y += dy;
        z += dz;
        return this;
    }

    public Event translate(Point3D vector) {
        return translate(vector.x, vector.y, vector.z);
    }

    public Event shiftTime(double dt) {
        t += dt;
        return this;
    }

    public Event scale(double scale) {
        x *= scale;
        y *= scale;
        z *= scale;
        return this;
    }

    public Event copyFrame() {
        return new Event(x, y, z, t, juggler, hand);
    }

    public void addManipulation(Manipulation m) {
        manipulations.add(m);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("<event x=\"%.4f\" y=\"%.4f\" z=\"%.4f\" t=\"%.4f\" hand=\"%d:%s\">\n", x, y, z, t, juggler, hand));
        for (Manipulation m : manipulations)
            builder.append(m.toString()).append('\n');
        builder.append("</event>");

        return builder.toString();
    }
}