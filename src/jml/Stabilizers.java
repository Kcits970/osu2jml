package jml;

import java.util.*;
import math.Point3D;

import static jml.HitObjectConversionFunctions.FRAME_DISTANCE_SECONDS;

public class Stabilizers {
    static final double SHORT_THRESHOLD = 0.2;
    static final double LONG_THRESHOLD = 0.6;

    public static List<Event> getStabilizer(List<Event> group1, List<Event> group2, double jmlDuration) {
        double seconds = secondsBetweenTwoGroups(group1, group2, jmlDuration);

        if (seconds <= SHORT_THRESHOLD)
            return Collections.emptyList();
        else if (seconds <= LONG_THRESHOLD)
            return getEmptyEvents(
                    group1.getLast().getPosition(),
                    group1.getLast().hand,
                    group1.getLast().t + FRAME_DISTANCE_SECONDS,
                    secondsBetweenTwoGroups(group1, group2, jmlDuration) - SHORT_THRESHOLD - FRAME_DISTANCE_SECONDS,
                    jmlDuration
            );
        else
            return getEmptyEvents(
                    Event.createDefaultEvent(group1.getLast().hand).getPosition(),
                    group1.getLast().hand,
                    group1.getLast().t + SHORT_THRESHOLD,
                    secondsBetweenTwoGroups(group1, group2, jmlDuration) - 2*SHORT_THRESHOLD,
                    jmlDuration
            );
    }

    private static double secondsBetweenTwoGroups(List<Event> group1, List<Event> group2, double jmlDuration) {
        return (group2.getFirst().t > group1.getLast().t) ?
                group2.getFirst().t - group1.getLast().t :
                group2.getFirst().t - group1.getLast().t + jmlDuration;
    }

    private static List<Event> getEmptyEvents(Point3D position, String hand, double start, double duration, double jmlDuration) {
        List<Event> emptyEvents = new ArrayList<>();

        boolean terminate = false;
        for (double elapsedSeconds = 0; !terminate; elapsedSeconds += FRAME_DISTANCE_SECONDS) {
            if (elapsedSeconds >= duration) {
                elapsedSeconds = duration;
                terminate = true;
            }

            emptyEvents.add(new Event(position, start + elapsedSeconds, 1, hand));
        }

        if (start + duration >= jmlDuration)
            emptyEvents.stream()
                    .filter(event -> event.t >= jmlDuration)
                    .forEach(event -> event.t -= jmlDuration);
        return emptyEvents;
    }
}
