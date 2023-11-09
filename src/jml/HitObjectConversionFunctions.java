package jml;

import jml.siteswap.*;
import osu.*;
import math.GeometryFunctions;
import math.PolarCoordinate;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Math.*;
import static jml.ManipulationType.CATCH;
import static jml.ManipulationType.HOLDING;
import static jml.ManipulationType.THROW;

public class HitObjectConversionFunctions {
    static final double FPS = 30;
    static final double FRAME_DISTANCE_MILLIS = 1000 / FPS;
    static final double FRAME_DISTANCE_SECONDS = 1 / FPS;

    static final double MAX_RPM = 477;
    static final PolarCoordinate SPIN_ORIGIN = new PolarCoordinate(50, -PI/2);
    static final double SPIN_DIRECTION = -1; //-1: clockwise spin, 1: counterclockwise spin

    public static List<EventGroup> convertHitObjects(List<HitObject> objects, VanillaSiteswap siteswap, JugglerHandSequence sequence) {
        List<EventGroup> conversions = new ArrayList<>();
        SiteswapStateTracker stateTracker = new SiteswapStateTracker(siteswap, sequence);

        for (HitObject object : objects) {
            stateTracker.advanceState();
            conversions.add(
                    convertHitObject(
                            object,
                            new JugglerIdentifierSet(stateTracker.getAssignedJuggler(), stateTracker.getLastAssignedHand()),
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
            eventGroup.translate(osuConstants.SCREEN_WIDTH/2, 0, osuConstants.SCREEN_HEIGHT);
            eventGroup.scale(0.5);
            eventGroup.shiftTime(-startTime);
        }
    }

    public static List<Stablizer> getStablizers(List<EventGroup> conversions, EmptyThresholdSet emptyThresholdSet, double cycleDuration) {
        List<Stablizer> stablizers = new ArrayList<>();
        List<EventGroup> leftHandEventGroups = conversions.stream().filter(eventGroup -> eventGroup.getHand() == Hand.LEFT_HAND).collect(Collectors.toList());
        List<EventGroup> rightHandEventGroups = conversions.stream().filter(eventGroup -> eventGroup.getHand() == Hand.RIGHT_HAND).collect(Collectors.toList());

        for (int i = 0; i < leftHandEventGroups.size(); i++)
            stablizers.add(new Stablizer(leftHandEventGroups.get(i), leftHandEventGroups.get((i+1) % leftHandEventGroups.size()), emptyThresholdSet, cycleDuration));

        for (int i = 0; i < rightHandEventGroups.size(); i++)
            stablizers.add(new Stablizer(rightHandEventGroups.get(i), rightHandEventGroups.get((i+1) % rightHandEventGroups.size()), emptyThresholdSet, cycleDuration));

        return stablizers;
    }

    public static EventGroup convertHitObject(HitObject object, JugglerIdentifierSet jugglerIdentifierSet, Ball ball) {
        if (object instanceof HitCircle)
            return convertHitCircle((HitCircle) object, jugglerIdentifierSet, ball);

        if (object instanceof Slider)
            return convertSlider((Slider) object, jugglerIdentifierSet, ball);

        if (object instanceof Spinner)
            return convertSpinner((Spinner) object, jugglerIdentifierSet, ball);

        return null;
    }

    public static EventGroup convertHitCircle(HitCircle circle, JugglerIdentifierSet jugglerIdentifierSet, Ball ball) {
        Event catchEvent = new Event(
                osuCoordinateToJMLCoordinate(circle.x, circle.y),
                osuTimeToJMLTime(circle.time),
                jugglerIdentifierSet
        );

        Event throwEvent = catchEvent.clone();
        throwEvent.t += 0.001;

        if (ball != null) {
            catchEvent.addManipulation(new Manipulation(CATCH, ball));
            throwEvent.addManipulation(new Manipulation(THROW, ball));
        }

        return new EventGroup(Arrays.asList(catchEvent, throwEvent));
    }

    public static EventGroup convertSlider(Slider slider, JugglerIdentifierSet jugglerIdentifierSet, Ball ball) {
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
                    jugglerIdentifierSet
            );

            if (ball != null)
                sliderEvent.addManipulation(new Manipulation(elapsedMillis == 0 ? CATCH : terminate ? THROW : HOLDING, ball));

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

    public static EventGroup convertSpinner(Spinner spinner, JugglerIdentifierSet jugglerIdentifierSet, Ball ball) {
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
                    jugglerIdentifierSet
            );

            if (ball != null)
                spinnerEvent.addManipulation(new Manipulation(elapsedMillis == 0 ? CATCH : terminate ? THROW : HOLDING, ball));

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
                osuConstants.DEFAULT_CENTER
        );
    }

    public static double osuTimeToJMLTime(double osuTime) {
        //time in osu is in terms of milliseconds, time in jml is in terms of seconds.
        return osuTime / 1000;
    }

    public static JMLCoordinate osuCoordinateToJMLCoordinate(double x, double y) {
        return new JMLCoordinate(-x, 0, -y);
    }

    public static JMLCoordinate osuCoordinateToJMLCoordinate(Point2D.Double point) {
        return osuCoordinateToJMLCoordinate(point.x, point.y);
    }
}
