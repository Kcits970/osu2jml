package osu;

public class TimingPoint {
    static final int TIME_INDEX = 0;
    static final int BEATLENGTH_INDEX = 1;
    static final int UNINHERITED_INDEX = 6;

    public int time;
    public double beatLength;
    public boolean uninherited;
    public double sliderVelocityMultiplier; //referenced as "SV" in the osu wiki.

    public TimingPoint(String timingPointString) {
        String[] timingPointParameters = timingPointString.split(",");

        time = Integer.parseInt(timingPointParameters[TIME_INDEX]);
        beatLength = Double.parseDouble(timingPointParameters[BEATLENGTH_INDEX]);
        uninherited = timingPointParameters[UNINHERITED_INDEX].equals("1");
        sliderVelocityMultiplier = 1; //SV of an uninherited timing point is equal to 1.

        if (beatLength < -1000) //the slowest possible slider velocity multiplier value is 0.1.
            beatLength = -1000;
    }

    public void inheritFields(TimingPoint parent) {
        sliderVelocityMultiplier *= -100/beatLength;
        beatLength = parent.beatLength;
    }
}
