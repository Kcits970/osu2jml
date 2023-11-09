package jml;

import java.util.List;

public class EventGroup {
    List<Event> events;

    public EventGroup(List<Event> events) {
        this.events = events;
    }

    public void translate(double x, double y, double z) {
        for (Event e : events) {
            e.x += x;
            e.y += y;
            e.z += z;
        }
    }

    public void shiftTime(double t) {
        for (Event e : events)
            e.t += t;
    }

    public void scale(double scale) {
        for (Event e : events) {
            e.x *= scale;
            e.y *= scale;
            e.z *= scale;
        }
    }

    public String getHand() {
        return events.get(0).hand;
    }

    public double getStartTime() {
        return events.get(0).t;
    }

    public double getEndTime() {
        return events.get(events.size() - 1).t;
    }
}
