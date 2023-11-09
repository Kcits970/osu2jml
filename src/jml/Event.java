package jml;

import math.Point3D;

import java.util.List;
import java.util.ArrayList;

public class Event implements Cloneable {
    public double x, y, z, t;
    public int juggler;
    public String hand;

    List<Manipulation> manipulations;

    public Event(double x, double y, double z, double t, int juggler, String hand) {
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

    @Override
    public Event clone() {
        return new Event(x, y, z, t, juggler, hand);
    }

    public void addManipulation(Manipulation m) {
        manipulations.add(m);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("<event x=\"%.4f\" y=\"%.4f\" z=\"%.4f\" t=\"%.4f\" hand=\"%s:%s\"\n>", x, y, z, t, juggler, hand));

        for (Manipulation m : manipulations)
            builder.append(m.toString()).append('\n');

        builder.append("</event>");

        return builder.toString();
    }
}