package jml;

import java.util.ArrayList;
import java.util.List;

import static jml.HitObjectConversionFunctions.FRAME_DISTANCE_SECONDS;

public class Stablizer {
    static final double HOLD_THRESHOLD = 0.2;
    static final double RESET_THRESHOLD = 0.6;

    public EventGroup previousGroup;
    public EventGroup nextGroup;
    public double cycleDuration;
    List<Event> emptyEvents;

    public Stablizer(EventGroup previousGroup, EventGroup nextGroup, double cycleDuration) {
        this.previousGroup = previousGroup;
        this.nextGroup = nextGroup;
        this.cycleDuration = cycleDuration;
        emptyEvents = new ArrayList<>();

        generateEmptyEvents();
        cycleOvertimedEvents();
    }

    public double secondsBetweenTwoGroups() {
        double timeBetweenTwoGroups = nextGroup.getStartTime() - previousGroup.getEndTime();
        if (timeBetweenTwoGroups < 0)
            timeBetweenTwoGroups += cycleDuration;

        return timeBetweenTwoGroups;
    }

    public void generateEmptyEvents() {
        if (secondsBetweenTwoGroups() <= HOLD_THRESHOLD)
            return;
        else if (secondsBetweenTwoGroups() <= RESET_THRESHOLD)
            generateEmptyEventsHoldingAt(previousGroup.events.getLast());
        else
            generateEmptyEventsResetAt(previousGroup.events.getLast());
    }

    public void cycleOvertimedEvents() {
        for (Event e : emptyEvents)
            if (e.t > cycleDuration)
                e.t -= cycleDuration;
    }

    public void generateEmptyEventsHoldingAt(Event reference) {
        double duration = secondsBetweenTwoGroups() - HOLD_THRESHOLD;

        boolean terminate = false;
        for (double elapsedSeconds = FRAME_DISTANCE_SECONDS; !terminate; elapsedSeconds += FRAME_DISTANCE_SECONDS) {
            if (elapsedSeconds >= duration) {
                elapsedSeconds = duration;
                terminate = true;
            }

            Event emptyEvent = reference.clone();
            emptyEvent.t += elapsedSeconds;
            emptyEvents.add(emptyEvent);
        }
    }

    public void generateEmptyEventsResetAt(Event reference) {
        double resetStartSeconds = reference.t + HOLD_THRESHOLD;
        double resetEndSeconds = reference.t + secondsBetweenTwoGroups() - HOLD_THRESHOLD;

        boolean terminate = false;
        for (double currentSeconds = resetStartSeconds; !terminate; currentSeconds += FRAME_DISTANCE_SECONDS) {
            if (currentSeconds >= resetEndSeconds) {
                currentSeconds = resetEndSeconds;
                terminate = true;
            }

            Event emptyEvent = getDefaultEmptyEvent(reference.hand);
            emptyEvent.juggler = reference.juggler;
            emptyEvent.t = currentSeconds;
            emptyEvents.add(emptyEvent);
        }
    }

    public static Event getDefaultEmptyEvent(String hand) {
        return new Event("left".equals(hand) ? -25 : 25, 0, 0, 0, 1, hand);
    }
}
