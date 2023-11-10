package osu;

import math.*;
import osu.path.*;

import java.util.ArrayList;
import java.util.List;
import java.awt.geom.Point2D;

public class Slider extends HitObject {
    private static final int CURVEPARAMS_INDEX = 5;
    private static final int SLIDES_INDEX = 6;
    private static final int LENGTH_INDEX = 7;

    public int slides;
    public double definedLength;
    public double endTime;
    public SliderPath path;

    public Slider(String sliderString) {
        //This constructor creates a 'raw' slider.
        super(sliderString);

        String[] sliderParameters = sliderString.split(",");

        //Parsing the curve points.
        String[] curveParameters = sliderParameters[CURVEPARAMS_INDEX].split("\\|");
        char curveType = curveParameters[0].charAt(0);
        List<Point2D.Double> curvePoints = new ArrayList<>();
        curvePoints.add(new Point2D.Double(x,y));

        for (int i = 1; i < curveParameters.length; i++)
            curvePoints.add(new Point2D.Double(
                    Integer.parseInt(curveParameters[i].substring(0, curveParameters[i].indexOf(':'))),
                    Integer.parseInt(curveParameters[i].substring(curveParameters[i].indexOf(':') + 1))
            ));

        //Parsing the remaining fields.
        slides = Integer.parseInt(sliderParameters[SLIDES_INDEX]);
        definedLength = Double.parseDouble(sliderParameters[LENGTH_INDEX]);

        //Construction of the slider's actual path.
        if (curveType == 'L')
            path = new LinearPath(curvePoints.get(0), curvePoints.get(1));
        else if (curveType == 'P')
            if (GeometryFunctions.arePointsInALine(curvePoints.get(0), curvePoints.get(1), curvePoints.get(2)))
                path = new LinearPath(curvePoints.get(0), curvePoints.get(2));
            else
                path = new CircularPath(curvePoints.get(0), curvePoints.get(1), curvePoints.get(2));
        else if (curveType == 'B')
            path = (CompoundBezierPath.isCompound(curvePoints)) ?
                    new CompoundBezierPath(curvePoints) :
                    new BezierPath(curvePoints);
        else
            throw new RuntimeException("invalid slider path specifier");
    }

    public Slider(Slider rawSlider, double sliderMultiplier, TimingPoint timingPoint) {
        super(rawSlider.x, rawSlider.y, rawSlider.time);
        path = rawSlider.path;
        definedLength = rawSlider.definedLength;
        slides = rawSlider.slides;

        double duration = definedLength / (sliderMultiplier * 100 * timingPoint.sliderVelocityMultiplier) * timingPoint.beatLength;
        endTime = time + duration;
    }

    @Override
    public Point2D.Double endPosition() {
        return path.pointAtLength(definedLength);
    }

    @Override
    public double endTime() {
        return endTime;
    }

    @Override
    public void adjustSpeed(double percentage) {
        time *= 1/percentage;
        endTime *= 1/percentage;
    }

    @Override
    public void translate(double dx, double dy) {
        super.translate(dx, dy);
        path = path.translate(dx, dy);
    }

    @Override
    public void flip() {
        super.flip();
        path = path.flip();
    }
}
