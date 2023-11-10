package jml;

import jml.siteswap.*;
import math.*;
import osu.*;

import java.awt.geom.Point2D;
import java.util.*;

import static java.lang.Math.*;

public class HitObjectConversionFunctions {
    static final double FPS = 30;
    static final double FRAME_DISTANCE_MILLIS = 1000 / FPS;
    static final double FRAME_DISTANCE_SECONDS = 1 / FPS;

    static final double MAX_RPM = 477;
    static final PolarCoordinate SPIN_ORIGIN = new PolarCoordinate(50, -PI/2);
    static final double SPIN_DIRECTION = -1; //-1: clockwise spin, 1: counterclockwise spin
    static final int JUGGLER_ID = 1;

    public static List<List<Event>> convertHitObjects(List<HitObject> objects, String siteswap, String handSequence) {
        List<List<Event>> conversions = new ArrayList<>();
        PropStateTracker stateTracker = new PropStateTracker(siteswap);
        Iterator<String> handStateTracker = new HandSequence(handSequence).iterator();

        for (HitObject object : objects)
            conversions.add(convertHitObject(object, handStateTracker.next(), stateTracker.advanceState()));

        double startTime = conversions.getFirst().getFirst().t;
        for (List<Event> conversion : conversions)
            for (Event event : conversion) {
                event.translate(BeatmapConstants.SCREEN_WIDTH/2, 0, BeatmapConstants.SCREEN_HEIGHT);
                event.scale(0.5);
                event.shiftTime(-startTime);
            }

        return conversions;
    }

    public static List<List<Event>> getStablizers(List<List<Event>> conversions, double cycleDuration) {
        List<List<Event>> stabilizers = new ArrayList<>();
        List<List<Event>> leftHandEventGroups = conversions.stream().filter(conversion -> conversion.getFirst().hand.equals("left")).toList();
        List<List<Event>> rightHandEventGroups = conversions.stream().filter(conversion -> conversion.getFirst().hand.equals("right")).toList();

        for (int i = 0; i < leftHandEventGroups.size(); i++)
            stabilizers.add(Stabilizers.getStabilizer(leftHandEventGroups.get(i), leftHandEventGroups.get((i+1) % leftHandEventGroups.size()), cycleDuration));

        for (int i = 0; i < rightHandEventGroups.size(); i++)
            stabilizers.add(Stabilizers.getStabilizer(rightHandEventGroups.get(i), rightHandEventGroups.get((i+1) % rightHandEventGroups.size()), cycleDuration));

        return stabilizers;
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
                osuCoordinateToJMLCoordinate(circle.x, circle.y),
                osuTimeToJMLTime(circle.time),
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

            int nthSlide = (int) floor(elapsedMillis/singleSliderDuration);
            double relativePosition = elapsedMillis/singleSliderDuration - nthSlide;
            boolean reverse = nthSlide % 2 != 0;

            Event sliderEvent = new Event(
                    osuCoordinateToJMLCoordinate(getSliderCoordinateAt(slider, relativePosition, reverse)),
                    osuTimeToJMLTime(slider.time + elapsedMillis),
                    JUGGLER_ID,
                    hand
            );

            String manipulationType = elapsedMillis == 0 ? "catch" : terminate ? "throw" : "holding";
            paths.forEach(path -> sliderEvent.addManipulation(new Manipulation(manipulationType, path)));
            sliderEvents.add(sliderEvent);
        }

        return sliderEvents;
    }

    public static Point2D.Double getSliderCoordinateAt(Slider slider, double relativePosition, boolean reverse) {
        double lengthUntilCoordinate = (reverse) ?
                slider.length * (1 - relativePosition) :
                slider.length * relativePosition;

        return slider.path.getPointAtLength(lengthUntilCoordinate);
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
                    osuCoordinateToJMLCoordinate(getSpinCoordinateAt(elapsedMillis)),
                    osuTimeToJMLTime(spinner.time + elapsedMillis),
                    JUGGLER_ID,
                    hand
            );

            String manipulationType = elapsedMillis == 0 ? "catch" : terminate ? "throw" : "holding";
            paths.forEach(path -> spinnerEvent.addManipulation(new Manipulation(manipulationType, path)));
            spinnerEvents.add(spinnerEvent);
        }

        return spinnerEvents;
    }

    public static Point2D.Double getSpinCoordinateAt(double elapsedMillis) {
        /*
        The fastest possible spin speed in osu! is 477 rpm.
        The cursor needs to spin exactly 477 times in 60000 milliseconds.
        This is equivalent to spinning once in 60000/477 milliseconds.
        Given a time t (in milliseconds), the spin angle (theta) needs to satisfy:
            (theta : 2PI = t : 60000/477)
        Solving for theta, we get theta = 2PI * t / (60000/477)
        */

        double spinAngle = 2*PI * elapsedMillis / (60000/MAX_RPM);
        return GeometryFunctions.add2Points(
                new PolarCoordinate(SPIN_ORIGIN.r, SPIN_ORIGIN.theta + SPIN_DIRECTION*spinAngle).toCartesian(),
                BeatmapConstants.DEFAULT_CENTER
        );
    }

    public static double osuTimeToJMLTime(double osuTime) {
        //time in osu is in terms of milliseconds, time in jml is in terms of seconds.
        return osuTime / 1000;
    }

    public static Point3D osuCoordinateToJMLCoordinate(double x, double y) {
        return new Point3D(-x, 0, -y);
    }

    public static Point3D osuCoordinateToJMLCoordinate(Point2D.Double point) {
        return osuCoordinateToJMLCoordinate(point.x, point.y);
    }
}
