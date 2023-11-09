package jml;

import jml.siteswap.*;
import math.Point3D;
import osu.*;
import math.GeometryFunctions;
import math.PolarCoordinate;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Math.*;

public class HitObjectConversionFunctions {
    static final double FPS = 30;
    static final double FRAME_DISTANCE_MILLIS = 1000 / FPS;
    static final double FRAME_DISTANCE_SECONDS = 1 / FPS;

    static final double MAX_RPM = 477;
    static final PolarCoordinate SPIN_ORIGIN = new PolarCoordinate(50, -PI/2);
    static final double SPIN_DIRECTION = -1; //-1: clockwise spin, 1: counterclockwise spin
    static final int JUGGLER_ID = 1;

    public static List<EventGroup> convertHitObjects(List<HitObject> objects, VanillaSiteswap siteswap, String handSequence) {
        List<EventGroup> conversions = new ArrayList<>();
        SiteswapStateTracker stateTracker = new SiteswapStateTracker(siteswap, handSequence);

        for (HitObject object : objects) {
            stateTracker.advanceState();
            conversions.add(
                    convertHitObject(
                            object,
                            stateTracker.getLastAssignedHand(),
                            stateTracker.getThrownBall()
                    )
            );
        }

        polishConvertedHitObjects(conversions);
        return conversions;
    }

    public static void polishConvertedHitObjects(List<EventGroup> conversions) {
        double startTime = conversions.get(0).getStartTime();

        for (EventGroup eventGroup : conversions) {
            eventGroup.translate(BeatmapConstants.SCREEN_WIDTH/2, 0, BeatmapConstants.SCREEN_HEIGHT);
            eventGroup.scale(0.5);
            eventGroup.shiftTime(-startTime);
        }
    }

    public static List<Stablizer> getStablizers(List<EventGroup> conversions, EmptyThresholdSet emptyThresholdSet, double cycleDuration) {
        List<Stablizer> stablizers = new ArrayList<>();
        List<EventGroup> leftHandEventGroups = conversions.stream().filter(eventGroup -> eventGroup.getHand().equals("left")).collect(Collectors.toList());
        List<EventGroup> rightHandEventGroups = conversions.stream().filter(eventGroup -> eventGroup.getHand().equals("right")).collect(Collectors.toList());

        for (int i = 0; i < leftHandEventGroups.size(); i++)
            stablizers.add(new Stablizer(leftHandEventGroups.get(i), leftHandEventGroups.get((i+1) % leftHandEventGroups.size()), emptyThresholdSet, cycleDuration));

        for (int i = 0; i < rightHandEventGroups.size(); i++)
            stablizers.add(new Stablizer(rightHandEventGroups.get(i), rightHandEventGroups.get((i+1) % rightHandEventGroups.size()), emptyThresholdSet, cycleDuration));

        return stablizers;
    }

    public static EventGroup convertHitObject(HitObject object, String hand, int path) {
        if (object instanceof HitCircle)
            return convertHitCircle((HitCircle) object, hand, path);

        if (object instanceof Slider)
            return convertSlider((Slider) object, hand, path);

        if (object instanceof Spinner)
            return convertSpinner((Spinner) object, hand, path);

        return null;
    }

    public static EventGroup convertHitCircle(HitCircle circle, String hand, int path) {
        Event catchEvent = new Event(
                osuCoordinateToJMLCoordinate(circle.x, circle.y),
                osuTimeToJMLTime(circle.time),
                JUGGLER_ID,
                hand
        );

        Event throwEvent = catchEvent.clone();
        throwEvent.t += 0.001;

        if (path != 0) {
            catchEvent.addManipulation(new Manipulation("catch", path));
            throwEvent.addManipulation(new Manipulation("throw", path));
        }

        return new EventGroup(Arrays.asList(catchEvent, throwEvent));
    }

    public static EventGroup convertSlider(Slider slider, String hand, int path) {
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

            if (path != 0)
                sliderEvent.addManipulation(new Manipulation(elapsedMillis == 0 ? "catch" : terminate ? "throw" : "holding", path));

            sliderEvents.add(sliderEvent);
        }

        return new EventGroup(sliderEvents);
    }

    public static Point2D.Double getSliderCoordinateAt(Slider slider, double relativePosition, boolean reverse) {
        double lengthUntilCoordinate = (reverse) ?
                slider.length * (1 - relativePosition) :
                slider.length * relativePosition;

        return slider.path.getPointAtLength(lengthUntilCoordinate);
    }

    public static EventGroup convertSpinner(Spinner spinner, String hand, int path) {
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

            if (path != 0)
                spinnerEvent.addManipulation(new Manipulation(elapsedMillis == 0 ? "catch" : terminate ? "throw" : "holding", path));

            spinnerEvents.add(spinnerEvent);
        }

        return new EventGroup(spinnerEvents);
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
