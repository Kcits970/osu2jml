package jml;

import jml.siteswap.Hand;
import list.Lists;

import java.util.ArrayList;
import java.util.List;

import static jml.HitObjectConversionFunctions.FRAME_DISTANCE_SECONDS;

public class Stablizer {
    public EventGroup previousGroup;
    public EventGroup nextGroup;
    public EmptyThresholdSet emptyThresholdSet;
    public double cycleDuration;
    List<Event> emptyEvents;

    public Stablizer(EventGroup previousGroup, EventGroup nextGroup, EmptyThresholdSet emptyThresholdSet, double cycleDuration) {
        this.previousGroup = previousGroup;
        this.nextGroup = nextGroup;
        this.emptyThresholdSet = emptyThresholdSet;
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
        if (secondsBetweenTwoGroups() <= emptyThresholdSet.holdThreshold)
            return;
        else if (secondsBetweenTwoGroups() <= emptyThresholdSet.resetThreshold)
            generateEmptyEventsHoldingAt(Lists.lastElement(previousGroup.events));
        else
            generateEmptyEventsResetAt(Lists.lastElement(previousGroup.events));
    }

    public void cycleOvertimedEvents() {
        for (Event e : emptyEvents)
            if (e.t > cycleDuration)
                e.t -= cycleDuration;
    }

    public void generateEmptyEventsHoldingAt(Event reference) {
        double duration = secondsBetweenTwoGroups() - emptyThresholdSet.holdThreshold;

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
        double resetStartSeconds = reference.t + emptyThresholdSet.holdThreshold;
        double resetEndSeconds = reference.t + secondsBetweenTwoGroups() - emptyThresholdSet.holdThreshold;

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

    public static Event getDefaultEmptyEvent(Hand hand) {
        return new Event((hand == Hand.LEFT_HAND) ? -25 : 25, 0, 0, 0, null, hand);
    }
}
