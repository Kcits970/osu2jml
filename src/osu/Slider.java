package osu;

import osu.math.*;

import java.util.ArrayList;
import java.util.List;
import java.awt.geom.Point2D;

public class Slider extends HitObject {
    static final int CURVEPARAMS_INDEX = 5;
    static final int SLIDES_INDEX = 6;
    static final int LENGTH_INDEX = 7;
    static final int CURVE_TYPE_INDEX = 0;
    static final int CURVE_POINT_INDEX = 1;

    public char curveType;
    public List<Point2D.Double> curvePoints;
    public int slides;
    public double length;
    public SliderPath path;

    public Slider(String sliderString) {
        super(sliderString);

        String[] sliderParameters = sliderString.split(",");
        setupCurveParams(sliderParameters[CURVEPARAMS_INDEX]);
        setPath();

        slides = Integer.parseInt(sliderParameters[SLIDES_INDEX]);
        length = Double.parseDouble(sliderParameters[LENGTH_INDEX]);
        endPosition = path.getPointAtLength(length);
    }

    private void setupCurveParams(String curveParams) {
        String[] curveParameters = curveParams.split("\\|");

        curveType = curveParameters[CURVE_TYPE_INDEX].charAt(0);
        curvePoints = new ArrayList<>();
        curvePoints.add(new Point2D.Double(x, y));

        for (int i = CURVE_POINT_INDEX; i < curveParameters.length; i++) {
            String[] curvePointParameters = curveParameters[i].split(":");
            int curvePointX = Integer.parseInt(curvePointParameters[0]);
            int curvePointY = Integer.parseInt(curvePointParameters[1]);

            curvePoints.add(new Point2D.Double(curvePointX, curvePointY));
        }
    }

    private void setPath() {
        switch (curveType) {
            case 'L': //linear slider
                path = new LinearPath(curvePoints.get(0), curvePoints.get(1));
                break;
            case 'P': //perfect circle slider
                if (GeometryFunctions.are3PointsInALine(curvePoints.get(0), curvePoints.get(1), curvePoints.get(2)))
                    path = new LinearPath(curvePoints.get(0), curvePoints.get(2));
                else
                    path = new CircularPath(curvePoints.get(0), curvePoints.get(1), curvePoints.get(2));
                break;
            case 'B': //bezier slider
                path = (CompoundBezierPath.isCompound(curvePoints)) ?
                        new CompoundBezierPath(curvePoints) :
                        new BezierPath(curvePoints);
        }
    }

    public void setEndTime(TimingPoint timingPoint, double sliderMultiplier) {
        /*
            based on 'https://osu.ppy.sh/wiki/en/Client/File_formats/osu_%28file_format%29#sliders'
            "duration = length / (sliderMultiplier * 100 * SV) * beatLength"
        */
        double duration = length / (sliderMultiplier * 100 * timingPoint.sliderVelocityMultiplier) * timingPoint.beatLength;
        endTime = time + duration;
    }

    @Override
    public void flip() {
        super.flip();

        for (Point2D.Double curvePoint : curvePoints)
            curvePoint.y = osuConstants.SCREEN_HEIGHT - curvePoint.y;

        setPath();
    }

    @Override
    public void shift(Point2D.Double unit) {
        super.shift(unit);

        Point2D.Double shiftingVector = new Point2D.Double(unit.x * -stackLayer, unit.y * -stackLayer);

        for (Point2D.Double curvePoint : curvePoints) {
            curvePoint.x += shiftingVector.x;
            curvePoint.y += shiftingVector.y;
        }

        setPath();
        endPosition = path.getPointAtLength(length);
    }
}
