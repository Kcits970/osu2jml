package osu;

import java.util.List;

public class BeatmapFunctions {
    public static TimingPoint findFurthestTimingPointUntil(List<TimingPoint> timingPoints, double targetTime) {
        for (int i = timingPoints.size() - 1; i >= 0; i--)
            if (timingPoints.get(i).time <= targetTime)
                return timingPoints.get(i);

        return null;
    }
}
