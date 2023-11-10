package osu;

public class TimingPoint {
    static final int TIME_INDEX = 0;
    static final int BEATLENGTH_INDEX = 1;
    static final int UNINHERITED_INDEX = 6;

    public final int time;
    public final double beatLength;
    public final boolean inherited;
    public final double sliderVelocityMultiplier; //referenced as "SV" in the osu wiki.

    public TimingPoint(String timingPointString) {
        //osu! file format wiki on timing points:
        //https://osu.ppy.sh/wiki/en/Client/File_formats/osu_%28file_format%29#timing-points
        //This constructor returns a 'raw' timing point. Raw timing points must be 'refreshed' by the second constructor before use.

        String[] timingPointParameters = timingPointString.split(",");

        time = Integer.parseInt(timingPointParameters[TIME_INDEX]);
        beatLength = Double.parseDouble(timingPointParameters[BEATLENGTH_INDEX]);
        inherited = timingPointParameters[UNINHERITED_INDEX].equals("0");
        sliderVelocityMultiplier = 0;
    }

    public TimingPoint(TimingPoint rawTimingPoint, TimingPoint parent) {
        time = rawTimingPoint.time;
        inherited = rawTimingPoint.inherited;

        if (inherited) {
            beatLength = parent.beatLength;
            sliderVelocityMultiplier = Math.max(0.1, -100/rawTimingPoint.beatLength);
        }
        else {
            beatLength = rawTimingPoint.beatLength;
            sliderVelocityMultiplier = 1;
        }
    }
}
