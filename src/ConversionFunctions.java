import jml.*;
import jml.siteswap.*;
import math.*;
import osu.*;

import java.util.*;

public class ConversionFunctions {
    static final double FPS = 30;
    static final double FRAME_DISTANCE_MILLIS = 1000 / FPS;
    static final double FRAME_DISTANCE_SECONDS = 1 / FPS;

    static final double SHORT_THRESHOLD = 0.2;
    static final double LONG_THRESHOLD = 0.6;

    static final double MAX_RPM = 477;
    static final PolarCoordinate SPIN_ORIGIN = new PolarCoordinate(50, -Math.PI/2);
    static final double SPIN_DIRECTION = -1; //-1: clockwise spin, 1: counterclockwise spin
    static final int JUGGLER_ID = 1;

    public static List<Event> convertHitObjects(List<HitObject> objects, String siteswap, String handSequence, double filler) {
        List<List<Event>> conversions = new ArrayList<>();
        PropStateTracker stateTracker = new PropStateTracker(siteswap);
        Iterator<String> handStateTracker = new HandSequence(handSequence).iterator();

        //Conversion of each osu hit object
        for (HitObject object : objects)
            conversions.add(convertHitObject(object, handStateTracker.next(), stateTracker.advanceState()));

        //Shifting the position and time of the events to make them appear more centered.
        double startTime = conversions.getFirst().getFirst().t;
        for (List<Event> conversion : conversions)
            for (Event event : conversion) {
                event.translate(BeatmapConstants.SCREEN_WIDTH/2, 0, BeatmapConstants.SCREEN_HEIGHT);
                event.scale(0.5);
                event.shiftTime(-startTime);
            }

        //If two events in a jml ladder diagram are "far" apart, the juggler's hands will "stride" far away.
        //We add "stabilizers" to keep the juggler's hands in position.
        double cycleDuration = conversions.getLast().getLast().t + filler;
        List<List<Event>> stabilizers = new ArrayList<>();
        List<List<Event>> leftHandEventGroups = conversions.stream().filter(conversion -> conversion.getFirst().hand.equals("left")).toList();
        List<List<Event>> rightHandEventGroups = conversions.stream().filter(conversion -> conversion.getFirst().hand.equals("right")).toList();

        for (int i = 0; i < leftHandEventGroups.size(); i++)
            stabilizers.add(getStabilizer(leftHandEventGroups.get(i), leftHandEventGroups.get((i+1) % leftHandEventGroups.size()), cycleDuration));

        for (int i = 0; i < rightHandEventGroups.size(); i++)
            stabilizers.add(getStabilizer(rightHandEventGroups.get(i), rightHandEventGroups.get((i+1) % rightHandEventGroups.size()), cycleDuration));

        conversions.addAll(stabilizers);

        //flatten the conversions to individual events, and sort them by time!
        List<Event> flattenedAndSorted = new ArrayList<>();
        conversions.forEach(flattenedAndSorted::addAll);
        flattenedAndSorted.sort(Comparator.comparingDouble(event -> event.t));

        return flattenedAndSorted;
    }

    public static List<Event> convertHitObject(HitObject object, String hand, Set<Integer> paths) {
        if (object instanceof HitCircle)
            return convertHitCircle((HitCircle) object, hand, paths);

        if (object instanceof Slider)
            return convertSlider((Slider) object, hand, paths);

        if (object instanceof Spinner)
            return convertSpinner((Spinner) object, hand, paths);

        return null;
    }

    public static List<Event> convertHitCircle(HitCircle circle, String hand, Set<Integer> paths) {
        Event catchEvent = new Event(
                toXYZCoordinate(circle.x, circle.y),
                toSeconds(circle.time),
                JUGGLER_ID,
                hand
        );
        paths.forEach(path -> catchEvent.addManipulation(new Manipulation("catch", path)));

        Event releaseEvent = catchEvent.copyFrame();
        releaseEvent.t += 0.001;
        paths.forEach(path -> releaseEvent.addManipulation(new Manipulation("throw", path)));

        return List.of(catchEvent, releaseEvent);
    }

    public static List<Event> convertSlider(Slider slider, String hand, Set<Integer> paths) {
        List<Event> sliderEvents = new ArrayList<>();
        double singleSliderDuration = slider.endTime - slider.time;
        double completeSliderDuration = singleSliderDuration * slider.slides;

        boolean terminate = false;
        for (double elapsedMillis = 0; !terminate; elapsedMillis += FRAME_DISTANCE_MILLIS) {
            if (elapsedMillis >= completeSliderDuration) {
                elapsedMillis = completeSliderDuration;
                terminate = true;
            }

            Event sliderEvent = new Event(
                    toXYZCoordinate(slideCoordinate(slider, elapsedMillis)),
                    toSeconds(slider.time + elapsedMillis),
                    JUGGLER_ID,
                    hand
            );

            String manipulationType = elapsedMillis == 0 ? "catch" : terminate ? "throw" : "holding";
            paths.forEach(path -> sliderEvent.addManipulation(new Manipulation(manipulationType, path)));
            sliderEvents.add(sliderEvent);
        }

        return sliderEvents;
    }

    private static Point2D slideCoordinate(Slider slider, double elapsedMillis) {
        double singleSliderDuration = slider.endTime - slider.time;

        int nthSlide = (int) Math.floor(elapsedMillis/singleSliderDuration);
        double relativePosition = elapsedMillis/singleSliderDuration - nthSlide;
        boolean reverse = nthSlide % 2 != 0;

        double lengthUntilCoordinate = (reverse) ?
                slider.definedLength * (1 - relativePosition) :
                slider.definedLength * relativePosition;

        return slider.path.pointAtLength(lengthUntilCoordinate);
    }

    public static List<Event> convertSpinner(Spinner spinner, String hand, Set<Integer> paths) {
        List<Event> spinnerEvents = new ArrayList<>();
        double spinnerDuration = spinner.endTime - spinner.time;

        boolean terminate = false;
        for (double elapsedMillis = 0; !terminate; elapsedMillis += FRAME_DISTANCE_MILLIS) {
            if (elapsedMillis >= spinnerDuration) {
                elapsedMillis = spinnerDuration;
                terminate = true;
            }

            Event spinnerEvent = new Event(
                    toXYZCoordinate(spinCoordinate(elapsedMillis)),
                    toSeconds(spinner.time + elapsedMillis),
                    JUGGLER_ID,
                    hand
            );

            String manipulationType = elapsedMillis == 0 ? "catch" : terminate ? "throw" : "holding";
            paths.forEach(path -> spinnerEvent.addManipulation(new Manipulation(manipulationType, path)));
            spinnerEvents.add(spinnerEvent);
        }

        return spinnerEvents;
    }

    private static Point2D spinCoordinate(double elapsedMillis) {
        /*
        The fastest possible spin speed in osu! is 477 rpm.
        The cursor needs to spin exactly 477 times in 60000 milliseconds.
        This is equivalent to spinning once in 60000/477 milliseconds.
        Given a time t (in milliseconds), the spin angle (theta) needs to satisfy:
            (theta : 2PI = t : 60000/477)
        Solving for theta, we get theta = 2PI * t / (60000/477)
        */

        double spinAngle = 2*Math.PI * elapsedMillis / (60000/MAX_RPM);
        return new PolarCoordinate(SPIN_ORIGIN.r, SPIN_ORIGIN.theta + SPIN_DIRECTION*spinAngle)
                .toCartesian()
                .shift(Spinner.DEFAULT_X, Spinner.DEFAULT_Y);
    }

    public static double toSeconds(double milliseconds) {
        return milliseconds / 1000;
    }

    public static Point3D toXYZCoordinate(double x, double y) {
        return new Point3D(-x, 0, -y);
    }

    public static Point3D toXYZCoordinate(Point2D point) {
        return toXYZCoordinate(point.x, point.y);
    }

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